package csw.subsync.payment.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.SetupIntent;
import csw.subsync.common.exception.PaymentException;
import csw.subsync.subscription.model.Membership;
import csw.subsync.subscription.model.SubscriptionGroup;
import csw.subsync.user.model.User;
import csw.subsync.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final StripeService stripeService;
    private final UserRepository userRepository;

    private String fetchStripePaymentMethodId(User user) {
        // retrieve from user's payment info in the DB
        String paymentMethodId = user.getPaymentMethodId();
        if (paymentMethodId == null || paymentMethodId.isBlank()) {
            throw new PaymentException("No paymentMethodId found for user " + user.getId());
        }
        return paymentMethodId;
    }

    public String createPaymentSetupSession(Long userId) throws StripeException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create customer if not exists
        if (user.getStripeCustomerId() == null) {
            Customer customer = stripeService.createCustomer(user.getEmail());
            user.setStripeCustomerId(customer.getId());
            userRepository.save(user);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("customer", user.getStripeCustomerId());
        params.put("usage", "off_session");
        params.put("metadata", Map.of("user_id", user.getId()));

        SetupIntent setupIntent = SetupIntent.create(params);
        return setupIntent.getClientSecret();
    }

    public PaymentIntent initiateSubscriptionPayment(Membership membership) throws StripeException {
        User user = membership.getUser();
        String customerId = user.getStripeCustomerId();
        if (customerId == null || customerId.isEmpty()) {
            throw new RuntimeException("User has no Stripe customer ID");
        }

        SubscriptionGroup group = membership.getSubscriptionGroup();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("membership_id", membership.getId().toString());
        metadata.put("group_id", group.getId().toString());

        return stripeService.createPaymentIntent(
                calculateAmount(group),
                "JPY",
                user.getStripeCustomerId(),
                user.getPaymentMethodId(),
                metadata
        );
    }

    private int calculateAmount(SubscriptionGroup group) {
        int amount = switch (group.getPricingModel()) {
            case FIXED -> group.getPriceAmount();
            case PER_MEMBER -> group.getPriceAmount() * group.getMemberships().size();
            case TIERED -> calculateTieredPrice(group);
            case DAILY_RATE -> group.getPriceAmount() * group.getDurationDays();
            default -> throw new IllegalStateException("Unknown pricing model");
        };

        validateAmount(amount, Currency.getInstance(group.getPriceCurrency()));
        return amount;
    }

    private int calculateTieredPrice(SubscriptionGroup group) {
        int basePrice = 15_00; // €15.00 base price
        int members = group.getMaxMembers();

        if (members <= 2) {
            return basePrice;
        } else if (members <= 5) {
            return basePrice + 5_00 * (members - 2);
        } else {
            return basePrice + 10_00 * (members - 2);
        }
    }

    private void validateAmount(int amount, Currency currency) {
        int minAmount = switch (currency.getCurrencyCode()) {
            case "EUR" -> 5;
            case "USD" -> 6;
            case "GBP" -> 7;
            default -> 5;
        };

        if (amount < minAmount) {
            throw new PaymentException("Amount " + (amount/100.0) +
                    " is below minimum for currency " + currency);
        }
    }
}
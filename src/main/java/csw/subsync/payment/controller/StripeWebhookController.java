package csw.subsync.payment.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.SetupIntent;
import csw.subsync.payment.service.StripeService;
import csw.subsync.subscription.service.MembershipService;
import csw.subsync.user.model.User;
import csw.subsync.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/stripe/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {
    private final StripeService stripeService;
    private final MembershipService membershipService;
    private final UserRepository userRepository;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = stripeService.constructWebhookEvent(payload, sigHeader, webhookSecret);

            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentSuccess(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentFailure(event);
                    break;
                case "setup_intent.succeeded":
                    handleSetupIntentSuccess(event);
                    break;
            }

            return ResponseEntity.ok().build();
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private void handlePaymentSuccess(Event event) {
        PaymentIntent intent = (PaymentIntent) event.getData().getObject();
        Long membershipId = Long.parseLong(intent.getMetadata().get("membership_id"));
        membershipService.handleSuccessfulPayment(membershipId, intent.getId());
    }

    private void handlePaymentFailure(Event event) {
        PaymentIntent intent = (PaymentIntent) event.getData().getObject();
        Long membershipId = Long.parseLong(intent.getMetadata().get("membership_id"));
        membershipService.handleFailedPayment(membershipId,
                intent.getLastPaymentError().getMessage());
    }

    private void handleSetupIntentSuccess(Event event) throws StripeException {
        SetupIntent setupIntent = (SetupIntent) event.getData().getObject();
        String userId = setupIntent.getMetadata().get("user_id");

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify the payment method is attached to our customer
        if (setupIntent.getCustomer() == null ||
                !setupIntent.getCustomer().equals(user.getStripeCustomerId())) {
            log.warn("Invalid customer for setup intent: {}", setupIntent.getId());
            return;
        }

        // Set as default payment method
        stripeService.setDefaultPaymentMethod(
                user.getStripeCustomerId(),
                setupIntent.getPaymentMethod()
        );

        user.setPaymentMethodId(setupIntent.getPaymentMethod());
        userRepository.save(user);
    }
}
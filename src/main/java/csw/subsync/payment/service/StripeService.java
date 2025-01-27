package csw.subsync.payment.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        // Initialize Stripe with our secret key
        Stripe.apiKey = secretKey;
    }

    public Customer createCustomer(String email) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        return Customer.create(params);
    }

    public PaymentIntent createPaymentIntent(long amount, String currency, String customerId,
                                             String paymentMethodId, Map<String, String> metadata) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount);
        params.put("currency", currency.toLowerCase());
        params.put("customer", customerId);
        params.put("payment_method", paymentMethodId);
        params.put("confirm", true);
        params.put("metadata", metadata);
        params.put("automatic_payment_methods", Map.of(
                "enabled", true,
                "allow_redirects", "never"
        ));

        return PaymentIntent.create(params);
    }

    public void setDefaultPaymentMethod(String customerId, String paymentMethodId)
            throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("invoice_settings", Map.of(
                "default_payment_method", paymentMethodId
        ));
        Customer customer = Customer.retrieve(customerId);
        customer.update(params);
    }

    public Event constructWebhookEvent(String payload, String sigHeader, String webhookSecret) throws StripeException {
        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
    }
}
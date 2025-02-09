package csw.subsync.payment.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    /**
     * When retrying the same operation, the metadata should contain the same values so that the idempotency key remains constant.
     * Ensure that metadata includes a unique identifier for the payment operation (e.g., order_id).
     */
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

        // Use a unique idempotency key
        RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey(generateUniqueKeyForPaymentIntent(metadata))
                .build();

        return PaymentIntent.create(params, requestOptions);
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

    /**
     * Generates a secure idempotency key for PaymentIntent operations.
     *
     * This implementation expects the metadata to contain both a "membership_id" and an "order_id"
     * (or any other unique identifier that represents the specific payment operation).
     *
     * Using both ensures that:
     * - The key is unique to a specific operation.
     * - Retries of the same operation produce the same key.
     *
     * order_id from membership
     */
    private String generateUniqueKeyForPaymentIntent(Map<String, String> metadata) {
        String membershipId = metadata.get("membership_id");
        String orderId = metadata.get("order_id");  // Must be provided and constant per operation

        if (membershipId == null || orderId == null) {
            throw new IllegalArgumentException("Missing required metadata: membership_id and order_id are required.");
        }

        String rawKey = "payment_intent_" + membershipId + "_" + orderId;

        // Generate a SHA-256 hash of the raw key to ensure a fixed-length, secure idempotency key.
        return sha256Hex(rawKey);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
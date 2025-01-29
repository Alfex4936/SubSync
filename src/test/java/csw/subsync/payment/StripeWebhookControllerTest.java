package csw.subsync.payment;

import com.stripe.events.V1BillingMeterErrorReportTriggeredEvent;
import com.stripe.events.V1BillingMeterNoMeterFoundEvent;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.SetupIntent;
import com.stripe.net.ApiResource;
import csw.subsync.payment.controller.StripeWebhookController;
import csw.subsync.payment.service.StripeService;
import csw.subsync.subscription.service.MembershipService;
import csw.subsync.user.model.User;
import csw.subsync.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripeWebhookControllerTest {

    @Mock private StripeService stripeService;
    @Mock private MembershipService membershipService;
    @Mock private UserRepository userRepository;

    @InjectMocks private StripeWebhookController stripeWebhookController;

    private final String webhookSecret = "testWebhookSecret"; // Use a test secret

    @Test
    void handleWebhook_paymentIntentSucceeded_success() throws StripeException {
        String payload = "{\"type\": \"payment_intent.succeeded\", \"data\": {\"object\": {\"id\": \"pi_123\", \"metadata\": {\"membership_id\": \"1\"}}}}";
        String sigHeader = "validSignature";
        Event mockEvent = mock(Event.class);
        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        V1BillingMeterErrorReportTriggeredEvent.EventData mockEventData = mock(V1BillingMeterErrorReportTriggeredEvent.EventData.class);
        EventDataObjectDeserializer mockDeserializer = mock(EventDataObjectDeserializer.class);

        when(stripeService.constructWebhookEvent(payload, sigHeader, webhookSecret)).thenReturn(mockEvent);
        when(mockEvent.getType()).thenReturn("payment_intent.succeeded");
        when(mockEvent.getData()).thenReturn(mockEventData); // Use EventData here
        when(mockEventData.getObjectDeserializer()).thenReturn(mockDeserializer); // Access deserializer from EventData
        when(mockDeserializer.getObject()).thenReturn(Optional.of(mockPaymentIntent));
        when(mockPaymentIntent.getMetadata()).thenReturn(Map.of("membership_id", "1"));
        when(mockPaymentIntent.getId()).thenReturn("pi_123");


        ResponseEntity<String> response = stripeWebhookController.handleWebhook(payload, sigHeader);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(membershipService).handleSuccessfulPayment(1L, "pi_123");
        verify(membershipService, never()).handleFailedPayment(anyLong(), anyString());
    }

    @Test
    void handleWebhook_paymentIntentPaymentFailed_success() throws StripeException {
        String payload = "{\"type\": \"payment_intent.payment_failed\", \"data\": {\"object\": {\"id\": \"pi_failed\", \"metadata\": {\"membership_id\": \"2\"}, \"last_payment_error\": {\"message\": \"Payment failed\"}}}}";
        String sigHeader = "validSignature";
        Event mockEvent = mock(Event.class);
        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        EventData mockEventData = mock(EventData.class);
        EventDataObjectDeserializer mockDeserializer = mock(EventDataObjectDeserializer.class);
        LastPaymentError mockLastError = mock(LastPaymentError.class); // Mock LastPaymentError

        when(stripeService.constructWebhookEvent(payload, sigHeader, webhookSecret)).thenReturn(mockEvent);
        when(mockEvent.getType()).thenReturn("payment_intent.payment_failed");
        when(mockEvent.getData()).thenReturn(mockEventData); // Use EventData here
        when(mockEventData.getObjectDeserializer()).thenReturn(mockDeserializer); // Access deserializer from EventData
        when(mockDeserializer.getObject()).thenReturn(Optional.of(mockPaymentIntent));
        when(mockPaymentIntent.getMetadata()).thenReturn(Map.of("membership_id", "2"));
        when(mockPaymentIntent.getId()).thenReturn("pi_failed");
        when(mockPaymentIntent.getLastPaymentError()).thenReturn(mockLastError); // Mock and return LastPaymentError
        when(mockLastError.getMessage()).thenReturn("Payment failed"); // Mock getMessage() on LastPaymentError


        ResponseEntity<String> response = stripeWebhookController.handleWebhook(payload, sigHeader);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(membershipService).handleFailedPayment(2L, "Payment failed");
        verify(membershipService, never()).handleSuccessfulPayment(anyLong(), anyString());
    }

    @Test
    void handleWebhook_setupIntentSucceeded_success() throws StripeException {
        String payload = "{\"type\": \"setup_intent.succeeded\", \"data\": {\"object\": {\"id\": \"seti_123\", \"metadata\": {\"user_id\": \"3\"}, \"customer\": \"cus_123\", \"payment_method\": \"pm_123\"}}}";
        String sigHeader = "validSignature";
        Event mockEvent = mock(Event.class);
        SetupIntent mockSetupIntent = mock(SetupIntent.class);
        EventData mockEventData = mock(EventData.class);
        EventDataObjectDeserializer mockDeserializer = mock(EventDataObjectDeserializer.class);
        User mockUser = mock(User.class);

        when(stripeService.constructWebhookEvent(payload, sigHeader, webhookSecret)).thenReturn(mockEvent);
        when(mockEvent.getType()).thenReturn("setup_intent.succeeded");
        when(mockEvent.getData()).thenReturn(mockEventData); // Use EventData here
        when(mockEventData.getObjectDeserializer()).thenReturn(mockDeserializer); // Access deserializer from EventData
        when(mockDeserializer.getObject()).thenReturn(Optional.of(mockSetupIntent));
        when(mockSetupIntent.getMetadata()).thenReturn(Map.of("user_id", "3"));
        when(mockSetupIntent.getCustomer()).thenReturn("cus_123");
        when(mockSetupIntent.getPaymentMethod()).thenReturn("pm_123");
        when(userRepository.findById(3L)).thenReturn(Optional.of(mockUser));
        when(mockUser.getStripeCustomerId()).thenReturn("cus_123");

        ResponseEntity<String> response = stripeWebhookController.handleWebhook(payload, sigHeader);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(stripeService).setDefaultPaymentMethod("cus_123", "pm_123");
        verify(userRepository).save(mockUser);
        verify(mockUser).setPaymentMethodId("pm_123");
    }

    @Test
    void handleWebhook_setupIntentSucceeded_invalidCustomer() throws StripeException {
        String payload = "{\"type\": \"setup_intent.succeeded\", \"data\": {\"object\": {\"id\": \"seti_123\", \"metadata\": {\"user_id\": \"3\"}, \"customer\": \"cus_wrong\", \"payment_method\": \"pm_123\"}}}";
        String sigHeader = "validSignature";
        Event mockEvent = mock(Event.class);
        SetupIntent mockSetupIntent = mock(SetupIntent.class);
        EventData mockEventData = mock(EventData.class);
        EventDataObjectDeserializer mockDeserializer = mock(EventDataObjectDeserializer.class);
        User mockUser = mock(User.class);

        when(stripeService.constructWebhookEvent(payload, sigHeader, webhookSecret)).thenReturn(mockEvent);
        when(mockEvent.getType()).thenReturn("setup_intent.succeeded");
        when(mockEvent.getData()).thenReturn(mockEventData); // Use EventData here
        when(mockEventData.getObjectDeserializer()).thenReturn(mockDeserializer); // Access deserializer from EventData
        when(mockDeserializer.getObject()).thenReturn(Optional.of(mockSetupIntent));
        when(mockSetupIntent.getMetadata()).thenReturn(Map.of("user_id", "3"));
        when(mockSetupIntent.getCustomer()).thenReturn("cus_wrong");
        when(mockSetupIntent.getPaymentMethod()).thenReturn("pm_123");
        when(userRepository.findById(3L)).thenReturn(Optional.of(mockUser));
        when(mockUser.getStripeCustomerId()).thenReturn("cus_123");

        ResponseEntity<String> response = stripeWebhookController.handleWebhook(payload, sigHeader);

        assertEquals(HttpStatus.OK, response.getStatusCode()); // Still returns OK, but logs a warning internally.
        verify(stripeService, never()).setDefaultPaymentMethod(anyString(), anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void handleWebhook_setupIntentSucceeded_userNotFound() throws StripeException {
        String payload = "{\"type\": \"setup_intent.succeeded\", \"data\": {\"object\": {\"id\": \"seti_123\", \"metadata\": {\"user_id\": \"999\"}, \"customer\": \"cus_123\", \"payment_method\": \"pm_123\"}}}";
        String sigHeader = "validSignature";
        Event mockEvent = mock(Event.class);
        SetupIntent mockSetupIntent = mock(SetupIntent.class);
        EventData mockEventData = mock(EventData.class);
        EventDataObjectDeserializer mockDeserializer = mock(EventDataObjectDeserializer.class);

        when(stripeService.constructWebhookEvent(payload, sigHeader, webhookSecret)).thenReturn(mockEvent);
        when(mockEvent.getType()).thenReturn("setup_intent.succeeded");
        when(mockEvent.getData()).thenReturn(mockEventData); // Use EventData here
        when(mockEventData.getObjectDeserializer()).thenReturn(mockDeserializer); // Access deserializer from EventData
        when(mockDeserializer.getObject()).thenReturn(Optional.of(mockSetupIntent));
        when(mockSetupIntent.getMetadata()).thenReturn(Map.of("user_id", "999"));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> stripeWebhookController.handleWebhook(payload, sigHeader));

        verify(stripeService, never()).setDefaultPaymentMethod(anyString(), anyString());
        verify(userRepository, never()).save(any());
    }


    @Test
    void handleWebhook_invalidEventType_success() throws StripeException {
        String payload = "{\"type\": \"unknown.event\", \"data\": {\"object\": {}}}";
        String sigHeader = "validSignature";
        Event mockEvent = mock(Event.class);

        when(stripeService.constructWebhookEvent(payload, sigHeader, webhookSecret)).thenReturn(mockEvent);
        when(mockEvent.getType()).thenReturn("unknown.event");

        ResponseEntity<String> response = stripeWebhookController.handleWebhook(payload, sigHeader);

        assertEquals(HttpStatus.OK, response.getStatusCode()); // Should still return OK for unknown events
    }

    @Test
    void handleWebhook_invalidSignature_badRequest() throws StripeException {
        String payload = "{\"type\": \"payment_intent.succeeded\", \"data\": {\"object\": {}}}";
        String invalidSigHeader = "invalidSignature";
        StripeException mockStripeException = mock(StripeException.class); // Mock StripeException

        when(stripeService.constructWebhookEvent(payload, invalidSigHeader, webhookSecret)).thenThrow(mockStripeException);

        ResponseEntity<String> response = stripeWebhookController.handleWebhook(payload, invalidSigHeader);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // We can't directly get message from mocked exception reliably, so just check status for now.
        // If needed, we could mock getMessage() on mockStripeException to assert on message as well.
        assertEquals(ResponseEntity.badRequest().build().getStatusCode(), response.getStatusCode());
    }

    @Test
    void handleWebhook_stripeExceptionDuringEventConstruction_badRequest() throws StripeException {
        String payload = "invalid payload";
        String sigHeader = "validSignature";
        StripeException mockStripeException = mock(StripeException.class); // Mock StripeException

        when(stripeService.constructWebhookEvent(payload, sigHeader, webhookSecret)).thenThrow(mockStripeException);

        ResponseEntity<String> response = stripeWebhookController.handleWebhook(payload, sigHeader);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // We can't directly get message from mocked exception reliably, so just check status for now.
        // If needed, we could mock getMessage() on mockStripeException to assert on message as well.
        assertEquals(ResponseEntity.badRequest().build().getStatusCode(), response.getStatusCode());
    }
}
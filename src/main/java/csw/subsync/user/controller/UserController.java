package csw.subsync.user.controller;

import com.stripe.exception.StripeException;
import csw.subsync.common.annotation.ApiV1;
import csw.subsync.payment.dto.PaymentSetupResponse;
import csw.subsync.payment.service.PaymentService;
import csw.subsync.user.doc.UserControllerDoc;
import csw.subsync.user.dto.UserProfileResponse;
import csw.subsync.user.model.User;
import csw.subsync.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ApiV1
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController implements UserControllerDoc {
    private final UserService userService;
    private final PaymentService paymentService;

    @Override
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @Override
    @PostMapping("/setup-stripe-session")
    public ResponseEntity<PaymentSetupResponse> createSetupSession() throws StripeException {
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        String clientSecret = paymentService.createPaymentSetupSession(user.getId());
        return ResponseEntity.ok(new PaymentSetupResponse(clientSecret));
    }
}

/*
https://dashboard.stripe.com/test/customers/

 */
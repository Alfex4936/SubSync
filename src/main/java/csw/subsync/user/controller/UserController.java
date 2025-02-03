package csw.subsync.user.controller;

import com.stripe.exception.StripeException;
import csw.subsync.common.annotation.ApiV1;
import csw.subsync.payment.dto.PaymentSetupResponse;
import csw.subsync.payment.service.PaymentService;
import csw.subsync.user.dto.UserProfileResponse;
import csw.subsync.user.model.User;
import csw.subsync.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ApiV1
@Tag(name = "유저", description = "유저 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final PaymentService paymentService;

    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @Operation(summary = "Create payment setup session",
            description = "Create a Stripe SetupIntent for adding payment methods")
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
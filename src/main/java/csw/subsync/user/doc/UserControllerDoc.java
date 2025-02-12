package csw.subsync.user.doc;

import com.stripe.exception.StripeException;
import csw.subsync.payment.dto.PaymentSetupResponse;
import csw.subsync.user.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

// Interface-based documentation for the user controller
@Tag(name = "User", description = "User API")
public interface UserControllerDoc {
    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필을 조회합니다.")
    @GetMapping("/me")
    ResponseEntity<UserProfileResponse> getMyProfile();

    @Operation(summary = "Create payment setup session",
            description = "Create a Stripe SetupIntent for adding payment methods")
    @PostMapping("/setup-stripe-session")
    ResponseEntity<PaymentSetupResponse> createSetupSession() throws StripeException;
}

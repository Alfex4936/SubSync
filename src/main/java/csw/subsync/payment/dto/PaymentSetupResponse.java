package csw.subsync.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record PaymentSetupResponse(
        @Schema(description = "Stripe 결제용 클라이언트 시크릿", example = "cs_test_abc123")
        String clientSecret
) {
}

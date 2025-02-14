package csw.subsync.payment.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record StripeChargeRequest(

        @Schema(description = "결제 설명(예: 주문 번호 등)", example = "Order #1234")
        String description,

        @Schema(description = "결제 금액(소수점 없는 센트 단위)", example = "1000")
        int amount,

        @Schema(description = "결제 통화 코드 (USD 또는 EUR)", example = "USD")
        Currency currency,

        @Schema(description = "Stripe 결제 시 사용자의 이메일", example = "user@example.com")
        String stripeEmail,

        @Schema(description = "Stripe에서 발급된 결제 토큰", example = "tok_abc12345")
        String stripeToken

) {
    public enum Currency {
        EUR, USD
    }
}
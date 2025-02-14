package csw.subsync.payment.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record PaymentMethodRequest(
        @Schema(description = "결제 수단 식별자 (예: pm_abc12345)", example = "pm_abc12345")
        String paymentMethodId
) {}
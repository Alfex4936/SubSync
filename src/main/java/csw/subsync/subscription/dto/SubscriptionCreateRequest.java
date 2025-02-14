package csw.subsync.subscription.dto;

import csw.subsync.subscription.model.PricingModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCreateRequest {

    @Schema(description = "Subscription의 제목", example = "Premium Membership")
    @NotBlank(message = "Title cannot be blank")
    private String title;

    @Schema(description = "최대 가입 가능 인원 수", example = "5")
    @Min(value = 1, message = "Max members must be at least 1")
    @Max(value = 10, message = "Max members cannot exceed 10")
    private int maxMembers;

    @Schema(description = "유효 기간(일 단위)", example = "7")
    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 30, message = "Duration cannot exceed 30 days")
    private int durationDays;

    @Schema(description = "가격 모델 선택(예: FLAT_RATE, TIERED 등)", example = "FLAT_RATE")
    @NotNull(message = "Pricing model cannot be null")
    private PricingModel pricingModel;

    @Schema(description = "미리 정의된 구독 정보가 있을 경우 사용되는 식별자", example = "100")
    private Long predefinedSubscriptionId;

    @Schema(description = "가격(정수), 0보다 커야 함", example = "1000")
    @Min(value = 1, message = "Price amount must be more than 0")
    @Max(value = Integer.MAX_VALUE, message = "Price amount is too high")
    private Integer priceAmount;

    @Schema(description = "3글자 대문자 ISO 통화 코드(e.g. USD)", example = "USD")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Price currency must be a 3-letter ISO code (e.g. USD)")
    private String priceCurrency;
}
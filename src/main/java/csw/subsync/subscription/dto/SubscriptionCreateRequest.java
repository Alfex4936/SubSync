package csw.subsync.subscription.dto;

import csw.subsync.subscription.model.PricingModel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// POST
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCreateRequest {
    @NotBlank(message = "Title cannot be blank")
    private String title;

    @Min(value = 1, message = "Max members must be at least 1")
    @Max(value = 10, message = "Max members cannot exceed 10")
    private int maxMembers;

    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 30, message = "Duration cannot exceed 30 days")
    private int durationDays;

    @NotNull(message = "Pricing model cannot be null")
    private PricingModel pricingModel;

    private Long predefinedSubscriptionId;

    // Price must be non-null if it’s required, and possibly > 0
    // @NotNull(message = "Price amount cannot be null")
    @Min(value = 1, message = "Price amount must be more than 0")
    @Max(value = Integer.MAX_VALUE, message = "Price amount is too high")
    private Integer priceAmount;

    // 3 uppercase letters only
    // @NotBlank(message = "Price currency cannot be blank")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Price currency must be a 3-letter ISO code (e.g. USD)")
    private String priceCurrency;
}
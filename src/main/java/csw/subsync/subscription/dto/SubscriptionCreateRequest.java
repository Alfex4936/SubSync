package csw.subsync.subscription.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

// POST
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCreateRequest {
    @NotNull(message = "Owner ID cannot be null")
    private Long ownerId;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @Min(value = 1, message = "Max members must be at least 1")
    @Max(value = 10, message = "Max members cannot exceed 10")
    private int maxMembers;

    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 30, message = "Duration cannot exceed 30 days")
    private int durationDays;
}
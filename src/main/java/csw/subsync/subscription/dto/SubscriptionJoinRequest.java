package csw.subsync.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionJoinRequest {

    @Schema(description = "가입할 그룹의 식별자", example = "1")
    @NotNull(message = "Group ID cannot be null")
    private Long groupId;
}
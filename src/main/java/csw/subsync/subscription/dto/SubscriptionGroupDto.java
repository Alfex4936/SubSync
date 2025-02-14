package csw.subsync.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;


@Data
@AllArgsConstructor
public class SubscriptionGroupDto {

    @Schema(description = "Subscription 그룹의 식별자", example = "1")
    private Long id;

    @Schema(description = "Subscription 그룹의 제목", example = "VIP Group")
    private String title;

    @Schema(description = "그룹 내 최대 가입 가능 인원 수", example = "10")
    private int maxMembers;

    @Schema(description = "유효 기간(일 단위)", example = "14")
    private int durationDays;

    @Schema(description = "Subscription 시작일", example = "2025-02-01")
    private LocalDate startDate;

    @Schema(description = "Subscription 종료일", example = "2025-02-14")
    private LocalDate endDate;

    @Schema(description = "현재 활성 상태 여부", example = "true")
    private boolean active;

    @Schema(description = "Subscription 그룹의 소유자 ID", example = "123")
    private Long ownerId;
}
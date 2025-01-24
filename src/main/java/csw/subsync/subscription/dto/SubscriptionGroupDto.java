package csw.subsync.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SubscriptionGroupDto {
    private Long id;
    private String title;
    private int maxMembers;
    private int durationDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private Long ownerId; // 소유자 id
}


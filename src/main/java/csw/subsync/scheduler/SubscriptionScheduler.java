package csw.subsync.scheduler;

import csw.subsync.subscription.model.Membership;
import csw.subsync.subscription.model.SubscriptionGroup;
import csw.subsync.subscription.repository.MembershipRepository;
import csw.subsync.subscription.repository.SubscriptionGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class SubscriptionScheduler {
    private final SubscriptionGroupRepository subscriptionGroupRepo;
    private final MembershipRepository membershipRepo;
    private final RedisTemplate<Object, Object> redisTemplate;

    /**
     * 1) 만료된 그룹 자동 비활성화
     * 매일 새벽 0시(예시)마다 endDate < 오늘인 그룹 중 active=true인 그룹 찾아서 inactive 처리
     */
    // 만료된 그룹 자동 비활성화: 트랜잭션 범위 유지
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void expireGroups() {
        LocalDate today = LocalDate.now();
        List<SubscriptionGroup> expiredGroups = subscriptionGroupRepo.findExpiredGroups(today);

        for (SubscriptionGroup group : expiredGroups) {
            group.setActive(false);
            subscriptionGroupRepo.save(group);
            redisTemplate.delete("subscription:" + group.getId());
            // TODO: 추가 작업 가능 (예: 알림 전송)
        }
    }

    /**
     * 2) 결제 실패 후 3일 지난 Membership 제거
     * 매일 새벽 1시(예시)마다 valid=false & paid=false & 실패일자 3일 이상 지난 멤버 제거
     */
    // 결제 실패 후 3일 지난 Membership 제거: 트랜잭션 유지
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void removeInvalidMembers() {
        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
        List<Membership> invalidMembers = membershipRepo.findAllInvalidMembersOlderThan(threeDaysAgo);

        membershipRepo.deleteAll(invalidMembers);
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public SubscriptionGroup getGroupById(Long groupId) {
        return subscriptionGroupRepo.findByIdAndActiveTrueWithMemberships(groupId);
    }
}

package csw.subsync.subscription.service;

import csw.subsync.common.exception.GroupNotEmptyException;
import csw.subsync.common.exception.GroupNotFoundException;
import csw.subsync.common.exception.NotGroupOwnerException;
import csw.subsync.subscription.model.Membership;
import csw.subsync.subscription.model.PricingModel;
import csw.subsync.subscription.model.SubscriptionGroup;
import csw.subsync.subscription.repository.MembershipRepository;
import csw.subsync.subscription.repository.SubscriptionGroupRepository;
import csw.subsync.user.model.User;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriptionService {
    private final SubscriptionGroupRepository subscriptionGroupRepo;
    private final MembershipRepository membershipRepo;
    private final MembershipService membershipService;

    private final RedisTemplate<Object, Object> redisTemplate;

    // 그룹 생성: 데이터 변경 작업이므로 트랜잭션 필요
    @Transactional
    public SubscriptionGroup createGroup(User owner, String title, int maxMembers, int durationDays,
                                         PricingModel pricingModel,
                                         Integer priceAmount,
                                         String priceCurrency) {
        SubscriptionGroup group = new SubscriptionGroup();
        group.setTitle(title);
        group.setMaxMembers(maxMembers);
        group.setDurationDays(durationDays);
        group.setActive(true);
        group.setOwner(owner);
        group.setStartDate(LocalDate.now());
        group.setEndDate(LocalDate.now().plusDays(durationDays));

        group.setPricingModel(pricingModel == null ? PricingModel.FIXED : pricingModel);
        group.setPriceAmount(priceAmount == null ? 0 : priceAmount);
        group.setPriceCurrency((priceCurrency == null || priceCurrency.isEmpty()) ? "USD" : priceCurrency);

        subscriptionGroupRepo.save(group);

        // Create a Membership for the owner
        Membership ownerMembership = new Membership();
        ownerMembership.setSubscriptionGroup(group);
        ownerMembership.setUser(owner);
        ownerMembership.setPaid(false);
        ownerMembership.setValid(true);
        membershipRepo.save(ownerMembership);
        group.getMemberships().add(ownerMembership);

        storeGroupInRedis(group); // Redis 캐싱
        return group;
    }

    // 그룹 가입: 동시성 문제 해결을 위해 비관적 락 사용
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public SubscriptionGroup joinGroup(Long groupId, User user) {
        SubscriptionGroup group = subscriptionGroupRepo.findByIdAndActiveTrueWithMemberships(groupId);
        if (group == null) {
            throw new RuntimeException("Group not found or inactive");
        }
        if (group.getMemberships().size() >= group.getMaxMembers()) {
            throw new RuntimeException("Group is full");
        }
        if (membershipRepo.existsByUserAndSubscriptionGroup(user, group)) {
            throw new RuntimeException("Already joined");
        }

        Membership m = new Membership();
        m.setUser(user);
        m.setSubscriptionGroup(group);
        m.setPaid(false);
        m.setValid(true);
        membershipRepo.save(m);

        group.getMemberships().add(m);
        if (group.getMemberships().size() == group.getMaxMembers()) {
            notifyFullGroup(group);
            chargeAllMembers(group.getId()); // Auto-trigger payments
        }

        return group;
    }

    // 그룹 삭제: 동시성 문제 해결을 위해 비관적 락 사용
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public void removeGroup(Long groupId) {
        SubscriptionGroup group = subscriptionGroupRepo.findByIdAndActiveTrueWithMemberships(groupId);
        if (group == null) throw new RuntimeException("Group not found");

        // We do NOT check group.getOwner() vs. user ID here anymore
        // because the @PreAuthorize check already guaranteed that condition
//        if (!group.getOwner().getId().equals(user.getId())) {
//            throw new NotGroupOwnerException("User is not the owner of the group");
//        }

        // Ensure the group is empty or only contains the owner
        User owner = group.getOwner();
        if (group.getMemberships().stream().anyMatch(m -> !m.getUser().equals(owner))) {
            throw new GroupNotEmptyException("Cannot delete group with other members");
        }

        membershipRepo.deleteAll(group.getMemberships());
        group.getMemberships().clear();
        group.setActive(false);
        subscriptionGroupRepo.save(group);

        deleteGroupFromRedis(groupId); // Redis 캐싱 제거
    }

    // 멤버십 요금 청구: 반복 작업에 대해 트랜잭션 분리
    public void chargeAllMembers(Long groupId) {
        SubscriptionGroup group = subscriptionGroupRepo.findByIdAndActiveTrueWithMemberships(groupId);
        if (group == null) {
            throw new GroupNotFoundException("Group not found or inactive");
        }

        // TODO: batch, if I wanna support more than...1000 members
        group.getMemberships().forEach(membershipService::processMembershipPayment);
//        group.getMemberships().forEach(member -> membershipService.chargeMember(member, group));
    }


    // 만료된 그룹 조회: 단순 조회 최적화
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<SubscriptionGroup> getExpiredGroups(LocalDate today) {
        return subscriptionGroupRepo.findExpiredGroups(today);
    }

    private void notifyFullGroup(SubscriptionGroup group) {
        // send mail or push
    }

    private void storeGroupInRedis(SubscriptionGroup group) {
        String key = "subscription:" + group.getId();
        redisTemplate.opsForValue().set(key, group.getId(), 35, TimeUnit.DAYS); // 35 days expiration
    }

    private void deleteGroupFromRedis(Long groupId) {
        String key = "subscription:" + groupId;
        redisTemplate.delete(key);
    }
}
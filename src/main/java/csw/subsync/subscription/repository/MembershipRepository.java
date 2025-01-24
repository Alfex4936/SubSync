package csw.subsync.subscription.repository;

import csw.subsync.subscription.model.Membership;
import csw.subsync.subscription.model.SubscriptionGroup;
import csw.subsync.user.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    boolean existsByUserAndSubscriptionGroup(User user, SubscriptionGroup subscriptionGroup);

    // 동시성이 높거나 데이터 충돌 가능성이 크면 비관적 락 사용 (안전성 확보)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Membership findByIdAndValidTrue(Long id);

    // 실패 일자를 저장하려면, Membership 엔티티에 failedDate 등의 필드가 필요
    // 여기서는 간단히 valid=false 이면서 3일 지난 멤버를 찾는다고 가정
    @Query("SELECT m FROM Membership m " +
            "WHERE m.valid = false AND m.paid = false " +
            "AND m.failedDate < :threeDaysAgo")
    List<Membership> findAllInvalidMembersOlderThan(@Param("threeDaysAgo") LocalDate threeDaysAgo);
}
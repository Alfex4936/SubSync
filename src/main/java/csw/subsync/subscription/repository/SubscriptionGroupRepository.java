package csw.subsync.subscription.repository;

import csw.subsync.subscription.model.SubscriptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SubscriptionGroupRepository extends JpaRepository<SubscriptionGroup, Long> {

    // 동시성이 높거나 데이터 충돌 가능성이 크면 비관적 락 사용 (안전성 확보)
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM SubscriptionGroup g LEFT JOIN FETCH g.memberships WHERE g.id = :id AND g.active = true")
    SubscriptionGroup findByIdAndActiveTrueWithMemberships(@Param("id") Long id);

    // 만료된(기간 지난) + 아직 active=true인 그룹 찾기
    @Query("SELECT g FROM SubscriptionGroup g WHERE g.active = true AND g.endDate < :today")
    List<SubscriptionGroup> findExpiredGroups(@Param("today") LocalDate today);
}
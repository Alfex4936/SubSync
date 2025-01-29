package csw.subsync.subscription.repository;

import csw.subsync.subscription.model.PredefinedSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PredefinedSubscriptionRepository extends JpaRepository<PredefinedSubscription, Long> {

}
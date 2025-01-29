package csw.subsync.subscription.service;

import csw.subsync.subscription.model.PredefinedSubscription;
import csw.subsync.subscription.repository.PredefinedSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PredefinedSubscriptionService {

    private final PredefinedSubscriptionRepository predefinedSubscriptionRepository;

    public List<PredefinedSubscription> getAllPredefinedSubscriptions() {
        return predefinedSubscriptionRepository.findAll();
    }

    public PredefinedSubscription getPredefinedSubscriptionById(Long id) {
        return predefinedSubscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Predefined Subscription not found")); // Custom exception is better
    }

    // Admin methods (if needed) - create, update, delete predefined subscriptions
    public PredefinedSubscription createPredefinedSubscription(PredefinedSubscription subscription) {
        return predefinedSubscriptionRepository.save(subscription);
    }

    public PredefinedSubscription updatePredefinedSubscription(Long id, PredefinedSubscription updatedSubscription) {
        PredefinedSubscription existingSubscription = getPredefinedSubscriptionById(id);
        existingSubscription.setName(updatedSubscription.getName());
        existingSubscription.setPriceAmount(updatedSubscription.getPriceAmount());
        existingSubscription.setPriceCurrency(updatedSubscription.getPriceCurrency());
        return predefinedSubscriptionRepository.save(existingSubscription);
    }

    public void deletePredefinedSubscription(Long id) {
        predefinedSubscriptionRepository.deleteById(id);
    }
}
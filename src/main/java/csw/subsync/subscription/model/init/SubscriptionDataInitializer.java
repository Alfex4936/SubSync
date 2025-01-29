package csw.subsync.subscription.model.init;

import csw.subsync.subscription.model.PredefinedSubscription;
import csw.subsync.subscription.repository.PredefinedSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionDataInitializer implements CommandLineRunner {

    private final PredefinedSubscriptionRepository predefinedSubscriptionRepository;

    @Override
    public void run(String... args) {
        if (predefinedSubscriptionRepository.count() == 0) {
            List<PredefinedSubscription> initialSubscriptions = List.of(
                    new PredefinedSubscription(null, "Netflix (ads)", 799, "USD"), // 7.99 USD
                    new PredefinedSubscription(null, "Netflix Standard", 1799, "USD"), // 17.99 USD
                    new PredefinedSubscription(null, "Netflix Premium", 2499, "USD"), // 24.99 USD

                    new PredefinedSubscription(null, "ChatGPT Plus", 20_00, "USD"), // 20.00 USD
                    new PredefinedSubscription(null, "ChatGPT Pro", 200_00, "USD"), // 200.00 USD

                    new PredefinedSubscription(null, "TVING Basic", 9_000, "KRW"),
                    new PredefinedSubscription(null, "TVING Standard", 12_500, "KRW"),
                    new PredefinedSubscription(null, "TVING Premium", 16_000, "KRW")
            );
            predefinedSubscriptionRepository.saveAll(initialSubscriptions);
            log.debug("Predefined subscriptions initialized.");
        } else {
            log.debug("Predefined subscriptions already exist.");
        }
    }
}
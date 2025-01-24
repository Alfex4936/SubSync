package csw.subsync.payment.service;

import csw.subsync.subscription.model.SubscriptionGroup;
import csw.subsync.user.model.User;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public boolean charge(User user, SubscriptionGroup group) {
        // mock Stripe usage
        // return true if success, else false
        return mockCharge(user, group);
    }

    private boolean mockCharge(User user, SubscriptionGroup group) {
        // pretend we call Stripe API
        // just return true for now
        return true;
    }
}
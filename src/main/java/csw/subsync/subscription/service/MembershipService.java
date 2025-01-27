package csw.subsync.subscription.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import csw.subsync.payment.service.PaymentService;
import csw.subsync.subscription.model.Membership;
import csw.subsync.subscription.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepo;
    private final PaymentService paymentService;

    @Transactional
    public void processMembershipPayment(Membership membership) {
        try {
            PaymentIntent intent = paymentService.initiateSubscriptionPayment(membership);
            membership.setPaymentStatus(Membership.PaymentStatus.PROCESSING);
            membership.setStripePaymentIntentId(intent.getId());
            membershipRepo.save(membership);
        } catch (StripeException e) {
            handlePaymentError(membership, e);
        }
    }

    @Transactional
    public void handleSuccessfulPayment(Long membershipId, String paymentIntentId) {
        Membership membership = membershipRepo.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Membership not found"));

        membership.setPaid(true);
        membership.setPaymentStatus(Membership.PaymentStatus.SUCCEEDED);
        membership.setFailedDate(null);
        membership.setValid(true);
        membership.setStripePaymentIntentId(paymentIntentId);
        membershipRepo.save(membership);
    }

    @Transactional
    public void handleFailedPayment(Long membershipId, String errorMessage) {
        Membership membership = membershipRepo.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Membership not found"));

        membership.setPaid(false);
        membership.setPaymentStatus(Membership.PaymentStatus.FAILED);
        membership.setFailedDate(LocalDate.now());
        membership.setValid(false);
        membershipRepo.save(membership);

        log.warn("Payment failed for membership {}: {}", membershipId, errorMessage);
    }

    private void handlePaymentError(Membership membership, Exception e) {
        membership.setPaid(false);
        membership.setPaymentStatus(Membership.PaymentStatus.FAILED);
        membership.setFailedDate(LocalDate.now());
        membership.setValid(false);
        membershipRepo.save(membership);

        log.error("Payment processing failed for membership {}: {}", membership.getId(), e.getMessage());
    }
}

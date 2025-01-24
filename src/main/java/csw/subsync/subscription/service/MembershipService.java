package csw.subsync.subscription.service;

import csw.subsync.common.exception.PaymentException;
import csw.subsync.payment.service.PaymentService;
import csw.subsync.subscription.model.Membership;
import csw.subsync.subscription.model.SubscriptionGroup;
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
    public void chargeMember(Membership member, SubscriptionGroup group) {
        try {
            boolean success = paymentService.charge(member.getUser(), group);
            if (success) {
                member.setPaid(true);
                member.setFailedDate(null);
            } else {
                member.setPaid(false);
                member.setFailedDate(LocalDate.now());
            }
            membershipRepo.save(member);
        } catch (PaymentException e) {
            log.error("Payment failed for user {}: {}", member.getUser().getId(), e.getMessage());
            member.setPaid(false);
            member.setFailedDate(LocalDate.now());
            member.setValid(false);
            membershipRepo.save(member);
        }
    }
}

package csw.subsync.subscription.model;


import csw.subsync.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "memberships",
        indexes = {
                @Index(name = "idx_memberships_user_id", columnList = "user_id"),
                @Index(name = "idx_memberships_subscription_group_id", columnList = "subscription_group_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private boolean paid;
    private boolean valid; // if user is kicked out or not
    private LocalDate failedDate; // if user failed to pay, this date is set

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_group_id")
    private SubscriptionGroup subscriptionGroup;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    public enum PaymentStatus {
        PROCESSING, SUCCEEDED, FAILED
    }
}

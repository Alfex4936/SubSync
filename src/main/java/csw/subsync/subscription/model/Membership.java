package csw.subsync.subscription.model;


import csw.subsync.user.model.User;
import jakarta.persistence.*;
import lombok.*;

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

    private boolean paid;
    private boolean valid; // if user is kicked out or not

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_group_id")
    private SubscriptionGroup subscriptionGroup;

    // TODO: update DB
    private LocalDate failedDate; // if user failed to pay, this date will be set
}

package csw.subsync.subscription.model;


import csw.subsync.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "subscription_groups",
        indexes = {
                @Index(name = "idx_sub_groups_active", columnList = "active"),
                @Index(name = "idx_sub_groups_owner_id", columnList = "owner_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    private int maxMembers;

    private int durationDays; // 1–30

    private LocalDate startDate;
    private LocalDate endDate;

    private boolean active; // indicates if subscription is active

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "subscriptionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Membership> memberships = new ArrayList<>();
}
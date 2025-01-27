package csw.subsync.subscription.model;


import csw.subsync.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    // lazy
    @OneToMany(mappedBy = "subscriptionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Membership> memberships = new ArrayList<>();

    @Column(name = "price_amount")
    private int priceAmount; // in cents

    @Column(name = "price_currency", length = 3)
    private String priceCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_model")
    private PricingModel pricingModel;
}
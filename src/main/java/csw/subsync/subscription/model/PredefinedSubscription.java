package csw.subsync.subscription.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "predefined_subscriptions",
        indexes = {
                @Index(name = "idx_predefined_subscriptions_name", columnList = "name", unique = true) // Explicit index on name
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PredefinedSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Netflix", "ChatGPT Pro"

    @Column(name = "price_amount")
    private int priceAmount; // Stored in cents (or smallest unit of currency)

    @Column(name = "price_currency", length = 3)
    private String priceCurrency; // e.g., "USD", "KRW"

    // TODO: Description, Logo URL, etc.
}
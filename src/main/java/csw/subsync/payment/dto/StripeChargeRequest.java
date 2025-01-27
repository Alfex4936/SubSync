package csw.subsync.payment.dto;

import lombok.Data;

@Data
public class StripeChargeRequest {
    public enum Currency {
        EUR, USD
    }

    private String description;
    private int amount;
    private Currency currency;
    private String stripeEmail;
    private String stripeToken;
}
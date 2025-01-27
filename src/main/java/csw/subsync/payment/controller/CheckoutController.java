package csw.subsync.payment.controller;

import csw.subsync.payment.dto.StripeChargeRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// TODO: remove
@Controller
public class CheckoutController {

    @Value("${stripe.public-key}")
    private String stripePublicKey;

    @GetMapping("/checkout")
    public String checkout(Model model) {
        // Example: 10 EUR
        model.addAttribute("amount", 10 * 100);
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("currency", StripeChargeRequest.Currency.EUR);
        return "checkout";
    }
}
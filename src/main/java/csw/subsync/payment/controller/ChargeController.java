package csw.subsync.payment.controller;

import com.stripe.exception.StripeException;
import csw.subsync.payment.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Controller
@RequiredArgsConstructor
public class ChargeController {

    private final StripeService stripeService;

//    @PostMapping("/charge")
//    public String charge(StripeChargeRequest request, Model model) throws StripeException {
//        request.setDescription("Example charge");
//        request.setCurrency(StripeChargeRequest.Currency.EUR);
//
//
//        Charge charge = stripeService.charge(request);
//        model.addAttribute("id", charge.getId());
//        model.addAttribute("status", charge.getStatus());
//        model.addAttribute("chargeId", charge.getId());
//        model.addAttribute("balance_transaction", charge.getBalanceTransaction());
//        return "result";
//    }

    @ExceptionHandler(StripeException.class)
    public String handleError(Model model, StripeException ex) {
        model.addAttribute("error", ex.getMessage());
        return "result";
    }
}
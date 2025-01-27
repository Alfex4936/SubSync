package csw.subsync.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}

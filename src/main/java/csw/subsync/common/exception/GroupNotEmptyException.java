package csw.subsync.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GroupNotEmptyException extends RuntimeException {
    public GroupNotEmptyException(String message) {
        super(message);
    }
}

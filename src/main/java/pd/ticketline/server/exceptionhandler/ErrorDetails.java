package pd.ticketline.server.exceptionhandler;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

public class ErrorDetails{
    private final String message;
    private final int code;

    public ErrorDetails(String message, HttpStatus status) {
        this.message = message;
        this.code = status.value();
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

}

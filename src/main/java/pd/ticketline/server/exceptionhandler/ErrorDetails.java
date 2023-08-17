package pd.ticketline.server.exceptionhandler;

import org.springframework.http.HttpStatus;

public class ErrorDetails {
    private final String message;
    private final int code;

    public ErrorDetails(String message, HttpStatus status) {
        this.message = message;
        this.code = status.value();
    }

    // Getters for message and code (you can add setters if needed)
    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

}

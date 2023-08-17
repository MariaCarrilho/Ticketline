package pd.ticketline.server.exceptionhandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice

public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException ex) {
        // Build a custom error response object
        ErrorDetails errorDetails = new ErrorDetails(ex.getMessage(), ex.getStatus());

        // You can log the exception details here if needed

        // Return the ResponseEntity with the error response object and the specified HTTP status
        return new ResponseEntity<>(errorDetails, ex.getStatus());
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Object> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        // Handle the HttpMediaTypeNotAcceptableException here, for example:
        ErrorDetails errorDetails = new ErrorDetails("Media type not acceptable", HttpStatus.NOT_ACCEPTABLE);
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_ACCEPTABLE);
    }}

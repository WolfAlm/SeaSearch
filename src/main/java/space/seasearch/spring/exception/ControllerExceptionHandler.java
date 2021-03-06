package space.seasearch.spring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import space.seasearch.spring.dto.ErrorDto;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(SeaSearchValidationException.class)
    protected ResponseEntity<Object> handleVerificationException(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(HttpStatus.BAD_REQUEST.value(), exception.getMessage()));
    }

    @ExceptionHandler(TelegramException.class)
    protected ResponseEntity<Object> handleTelegramException(TelegramException e) {
        return ResponseEntity.status(e.getStatus()).body(new ErrorDto(e.getStatus(), e.getMessage()));
    }

    @ExceptionHandler(TwoFaException.class)
    protected ResponseEntity<Object> handle2FaTelegramException(TwoFaException e){
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}

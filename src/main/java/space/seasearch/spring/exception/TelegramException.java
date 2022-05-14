package space.seasearch.spring.exception;

import lombok.Getter;

public class TelegramException extends Exception{

    @Getter
    private int status;

    public TelegramException(String message, int status) {
        super(message);
        this.status = status;
    }
}

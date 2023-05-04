package gamein2.schedule.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends Exception {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException() {}
}

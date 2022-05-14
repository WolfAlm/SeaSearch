package space.seasearch.spring.service;

import org.springframework.stereotype.Service;
import space.seasearch.spring.exception.SeaSearchValidationException;

import java.util.regex.Pattern;

@Service
public class TelegramAuthDtoValidator {

    private final static Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");
    private final static String PHONE_FIELD = "phone number";
    private final static String CODE_FIELD = "code field";
    private final static String PASSWORD_FIELD = "password field";

    public void validatePhoneNumber(String phoneNumber) throws Exception {
        validateNotNull(phoneNumber, PHONE_FIELD);
        validateNotBlank(phoneNumber, PHONE_FIELD);
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new SeaSearchValidationException("Phone number contains illegal characters");
        }
    }

    public void validateCode(String code) throws Exception {
        validateNotNull(code, CODE_FIELD);
        validateNotBlank(code, CODE_FIELD);
    }

    public void validatePassword(String password) throws Exception {
        validateNotNull(password, PASSWORD_FIELD);
        validateNotBlank(password, PASSWORD_FIELD);
    }

    private void validateNotBlank(String entity, String name) throws Exception {
        if (entity.isBlank() || entity.isEmpty())
            throw new SeaSearchValidationException("Given " + name + " is blank or empty");
    }


    private void validateNotNull(String entity, String name) throws Exception {
        if (entity == null)
            throw new SeaSearchValidationException("Given " + name + " is null");
    }
}

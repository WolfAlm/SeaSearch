package space.seasearch.spring.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class TelegramAuthDtoValidator {

    private final Pattern phonePattern = Pattern.compile("^\\d{10}$");
    private final String PHONE_FIELD = "phone number";
    private final String CODE_FIELD = "code field";
    private final String PASSWORD_FIELD = "password field";

    public void validatePhoneNumber(String phoneNumber) throws Exception {
        validateNotNull(phoneNumber, PHONE_FIELD);
        validateNotBlank(phoneNumber, PHONE_FIELD);
        if (!phonePattern.matcher(phoneNumber).matches()) {
            throw new Exception("Phone number contains illegal characters");
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
            throw new Exception("Given " + name + " is blank or empty");
    }


    private void validateNotNull(String entity, String name) throws Exception {
        if (entity == null)
            throw new Exception("Given " + name + " is null");
    }
}

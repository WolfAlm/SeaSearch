package space.seasearch.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import space.seasearch.spring.entity.SeaSearchUser;
import space.seasearch.spring.exception.TelegramException;
import space.seasearch.spring.exception.TwoFaException;
import space.seasearch.spring.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class TelegramAuthService {

    private final UserRepository userRepository;
    private final TGCacheService tgCacheService;

    public SeaSearchUser processNewUser(String phoneNumber) throws Exception {
        var tgClient = tgCacheService.getOrCreateClient(phoneNumber);

        if (!tgClient.isWaitingForPhoneNumber()) {
            throw new TelegramException("User with pone numebr " + phoneNumber + " is already in status " + tgClient.getCurrentStateConstructor(),
                    HttpStatus.BAD_REQUEST.value());
        }

        var latch = tgClient.startRequest();
        tgClient.authPhoneNumber(phoneNumber);
        latch.await();

        if (tgClient.hasError()) {
            throw tgClient.getException();
        }

        return registerUser(phoneNumber, tgClient.getToken());
    }

    public void authenticateUserWithCode(String phoneNumber, String code) throws Exception {
        var tgClient = tgCacheService.getOrCreateClient(phoneNumber);

        if (!tgClient.isWaitingCode()) {
            throw new TelegramException("User not waiting for code", HttpStatus.FORBIDDEN.value());
        }

        var latch = tgClient.startRequest();
        tgClient.authCode(code);
        latch.await();

        if (tgClient.hasError()) {
            userRepository.deleteById(phoneNumber);
            throw tgClient.getException();
        }

        if (tgClient.is2FA()) {
            throw new TwoFaException();
        }
    }

    public void authenticateWithPassword(String phoneNumber, String password) throws Exception {
        var tgClient = tgCacheService.getOrCreateClient(phoneNumber);

        if (!tgClient.isWaitingPassword()) {
            throw new TelegramException("User not waiting for password", HttpStatus.FORBIDDEN.value());
        }

        var latch = tgClient.startRequest();
        tgClient.authPassword(password);
        latch.await();

        if (tgClient.hasError()) {
            throw tgClient.getException();
        }
    }

    public SeaSearchUser registerUser(String phoneNumber, String token) throws Exception {
        if (userRepository.findById(phoneNumber).isPresent())
            throw new Exception("user with phone number {} already registered");

        var user = new SeaSearchUser();
        user.setPhoneNumber(phoneNumber);
        user.setTokenPath(token);
        return userRepository.save(user);
    }

    public void logoutUser(String phoneNumber) throws Exception {
        var tgClient = tgCacheService.getClientOrThrow(phoneNumber);
        var latch = tgClient.startRequest();
        tgClient.exitUser();
        latch.await();

        if (tgClient.hasError()) {
            throw tgClient.getException();
        }

        userRepository.deleteById(phoneNumber);
        tgCacheService.deleteUserByToken(phoneNumber);
    }
}

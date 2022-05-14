package space.seasearch.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import space.seasearch.spring.entity.SeaSearchUser;
import space.seasearch.spring.exception.TelegramException;
import space.seasearch.spring.repository.UserRepository;
import space.seasearch.telegram.user.UserClient;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TelegramAuthService {

    private final UserRepository userRepository;
    private final TGCacheService tgCacheService;

    public SeaSearchUser processNewUser(String phoneNumber) throws Exception {
        var tgClient = tgCacheService.getOrCreateClient(phoneNumber);
        var user = registerUser(phoneNumber, tgClient.getToken());
        user.setTokenPath(tgClient.getToken());


        if (!tgClient.isWaitingForPhoneNumber()) {
            throw new TelegramException("User with pone numebr " + phoneNumber + " is already in status " + tgClient.getCurrentStateConstructor());
        }

//        CompletableFuture.runAsync(()->{
//            tgClient.authPhoneNumber(phoneNumber);
//        }).join();

        var latch = tgClient.startRequest();
        tgClient.authPhoneNumber(phoneNumber);
        latch.await();

        return user;
    }

    public void authenticateUserWithCode(String userPhoneNumber, String code) throws Exception {
        var tgClient = tgCacheService.getOrCreateClient(userPhoneNumber);

        if (!tgClient.isWaitingCode()) {
            throw new TelegramException("User not waiting for code");
        }

        var latch = tgClient.startRequest();
        tgClient.authCode(code);
        latch.await();

        if (tgClient.hasError() || tgClient.is2FA()) {
            userRepository.deleteById(userPhoneNumber);
            throw new TelegramException(tgClient.getCurrentError());
        }
    }

    public void authenticateWithPassword(String userPhoneNumber, String password) throws Exception {
        var tgClient = tgCacheService.getOrCreateClient(userPhoneNumber);

        if (!tgClient.isWaitingPassword()) {
            throw new TelegramException("User not waiting for password");
        }

        var latch = tgClient.startRequest();
        tgClient.authPassword(password);
        latch.await();

        if (tgClient.hasError()) {
            throw new TelegramException(tgClient.getCurrentError());
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

    public void logoutUser(String userPhone) throws Exception {
        var tgClient = tgCacheService.getOrCreateClient(userPhone);
        var latch = tgClient.startRequest();
        tgClient.exitUser();
        latch.await();

        if (tgClient.hasError()) {
            throw new TelegramException("Logout failed with error message " + tgClient.getCurrentError());
        }

        userRepository.deleteById(userPhone);
    }
}

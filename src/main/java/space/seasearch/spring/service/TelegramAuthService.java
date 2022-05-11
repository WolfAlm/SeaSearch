package space.seasearch.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import space.seasearch.spring.entity.SeaSearchUser;
import space.seasearch.spring.repository.UserRepository;

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
            throw new Exception("User with pone numebr " + phoneNumber + " is already in status " + tgClient.getCurrentStateConstructor());
        }
        var latch = tgClient.startRequest();
        tgClient.authPhoneNumber(phoneNumber);
        latch.await();

        if (tgClient.hasError()) {
            throw new Exception("User Client error " + tgClient.getCurrentError());
        }

        return user;
    }

    public ResponseEntity authenticateUserWithCode(String userPhoneNumber, String code) throws Exception {
        var tgClient = tgCacheService.getOrCreateClient(userPhoneNumber);

        if (!tgClient.isWaitingCode()) {
            throw new Exception("User not waiting for code");
        }

        var latch = tgClient.startRequest();
        tgClient.authCode(code);
        latch.await();

        if (!tgClient.hasError()) {
            return ResponseEntity.ok().build();
        }

        if (tgClient.is2FA()) {
            return ResponseEntity.status(401).header("X-SeaSearch-2FA", "required").build();
        }

        return ResponseEntity.status(tgClient.getStatus()).body(tgClient.getCurrentError());

    }

    public SeaSearchUser registerUser(String phoneNumber, String token) throws Exception {
        if (userRepository.findById(phoneNumber).isPresent())
            throw new Exception("user with phone number {} already registered");
        var user = new SeaSearchUser();
        user.setUsername(phoneNumber);
        user.setTokenPath(token);
        return userRepository.save(user);
    }
}

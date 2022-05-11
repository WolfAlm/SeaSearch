package space.seasearch.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import space.seasearch.spring.entity.UserInfo;
import space.seasearch.spring.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class TelegramAuthService {

    private final UserRepository userRepository;
    private final TGCacheService tgCacheService;

    public UserInfo processNewUser(String phoneNumber) throws Exception {
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

    public UserInfo registerUser(String phoneNumber, String token) throws Exception {
        if (userRepository.findById(phoneNumber).isPresent())
            throw new Exception("user with phone number {} already registered");
        var user = new UserInfo();
        user.setUsername(phoneNumber);
        user.setTokenPath(token);
        return userRepository.save(user);
    }
}

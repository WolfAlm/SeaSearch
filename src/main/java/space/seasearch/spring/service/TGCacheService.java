package space.seasearch.spring.service;

import it.tdlight.common.TelegramClient;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import space.seasearch.telegram.user.TelegramClientFactory;
import space.seasearch.telegram.user.UserClient;

@Service
public class TGCacheService {

    private final Map<String, UserClient> userPhoneToClient = new ConcurrentHashMap<>();

    public boolean tokenExist(String token) {
        return userPhoneToClient.containsKey(token);
    }

    public UserClient findUserClientByPhone(String token) {
        return userPhoneToClient.getOrDefault(token, null);
    }

    public void deleteUserByToken(String token) {
        userPhoneToClient.remove(token);
    }

    public UserClient getOrCreateClient(String phoneNumber) throws InterruptedException {
        if (!userPhoneToClient.containsKey(phoneNumber)) {
            userPhoneToClient.put(phoneNumber, createUserClient());
        }
        return findUserClientByPhone(phoneNumber);
    }

    public boolean tokenIsPresent(Optional<String> token) {
        return token.isPresent() && tokenExist(token.get());
    }


    private UserClient createUserClient() throws InterruptedException {
        TelegramClient telegramClient = TelegramClientFactory.createClient();

        UserClient userClient = new UserClient(telegramClient);
        var latch = userClient.startRequest();
        userClient.start();
        latch.await();
        return userClient;
    }

}

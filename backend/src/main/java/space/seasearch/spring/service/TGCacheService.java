package space.seasearch.spring.service;

import it.tdlight.common.TelegramClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import org.springframework.stereotype.Service;
import space.seasearch.telegram.user.TelegramClientFactory;
import space.seasearch.telegram.user.UserClient;

@Service
public class TGCacheService {

    private final Map<String, UserClient> cachedClients = new HashMap<>();

    public boolean tokenExist(String token) {
        return cachedClients.containsKey(token);
    }

    public UserClient findUserClientByToken(String token) {
        return cachedClients.getOrDefault(token, null);
    }

    public void deleteUserByToken(String token) {
        cachedClients.remove(token);
    }

    public String generateToken() {
        String token = "" + UUID.randomUUID();
        return Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    public UserClient registerToken(String token, CountDownLatch countDownLatch) {
        if (!cachedClients.containsKey(token)) {
            cachedClients.put(token, createUserClient(token, countDownLatch));
        }
        return findUserClientByToken(token);
    }

    public boolean tokenIsPresent(Optional<String> token) {
        return token.isPresent() && tokenExist(token.get());
    }


    private UserClient createUserClient(String token, CountDownLatch countDownLatch) {
        TelegramClient telegramClient = TelegramClientFactory.createClient();

        UserClient userClient = new UserClient(telegramClient, token);
        userClient.setCountDownLatch(countDownLatch);
        userClient.start();
        return userClient;
    }

}

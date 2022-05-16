package space.seasearch.spring.service;

import it.tdlight.common.TelegramClient;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import space.seasearch.spring.exception.SeaSearchClientNotFoundException;
import space.seasearch.telegram.user.TelegramClientFactory;
import space.seasearch.telegram.client.UserClient;

@Service
@RequiredArgsConstructor
public class TGCacheService {

    private final Map<String, UserClient> userPhoneToClient = new ConcurrentHashMap<>();

    private final ApplicationEventPublisher applicationEventPublisher;

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
            userPhoneToClient.put(phoneNumber, createUserClient(phoneNumber));
        }
        return findUserClientByPhone(phoneNumber);
    }

    public UserClient getClientOrThrow(String phoneNumber) throws SeaSearchClientNotFoundException {
        if (!userPhoneToClient.containsKey(phoneNumber)) {
            throw new SeaSearchClientNotFoundException(phoneNumber);
        }
        return userPhoneToClient.get(phoneNumber);
    }

    private UserClient createUserClient(String phoneNumber) throws InterruptedException {
        TelegramClient telegramClient = TelegramClientFactory.createClient();

        UserClient userClient = new UserClient(telegramClient, applicationEventPublisher, phoneNumber);
        var latch = userClient.startRequest();
        userClient.start();
        latch.await();
        return userClient;
    }

}

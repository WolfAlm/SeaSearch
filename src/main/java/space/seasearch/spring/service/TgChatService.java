package space.seasearch.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import space.seasearch.spring.dto.ChatDto;
import space.seasearch.spring.dto.ChatInfoDto;
import space.seasearch.spring.exception.SeaSearchClientNotFoundException;
import space.seasearch.spring.mapper.ChatMapper;
import space.seasearch.spring.mapper.ChatInfoMapper;
import space.seasearch.spring.repository.UserRepository;
import space.seasearch.telegram.client.ChatClient;
import space.seasearch.telegram.client.ChatDataClient;
import space.seasearch.telegram.stats.info.InfoStats;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TgChatService {

    private final TGCacheService tgCacheService;
    private final UserService userService;
    private final ChatMapper chatMapper;
    private final ChatInfoMapper profileMappr;
    private final UserRepository userRepository;

    public List<ChatDto> getChats(String userPhone) throws SeaSearchClientNotFoundException, InterruptedException {
        var client = tgCacheService.getClientOrThrow(userPhone);

        var chatClient = new ChatClient(client.getClient());
        var latch = chatClient.startRequest();
        chatClient.getAllChats();
        latch.await();

        var chatIds = chatClient.getIds();
        userService.updateGroupIds(userPhone, chatIds);
        chatIds.forEach(id -> {
            var latch2 = chatClient.startRequest();
            chatClient.getChat(id);
            try {
                latch2.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        loadChatPhotos(chatClient);

        return chatClient.getChats()
                .values()
                .stream()
                .map(chatMapper::map)
                .toList();
    }

    public ChatInfoDto getChatData(String phoneNumber, long chatId) throws SeaSearchClientNotFoundException, InterruptedException {
        var client = tgCacheService.getClientOrThrow(phoneNumber);

        var chatClient = new ChatClient(client.getClient());

        var latch = chatClient.startRequest();
        chatClient.getChat(chatId);
        latch.await();

        var chatDataClient = new ChatDataClient(chatClient.getClient());

        chatDataClient.extractData(chatId, chatClient.getChats().get(chatId).lastMessage.id);

        userService.updateStats(chatDataClient.getStats(), phoneNumber, chatId);

        return profileMappr.map(chatDataClient.getStats());
    }

    public void updateChat(String phone, long chatId) throws SeaSearchClientNotFoundException, InterruptedException {
        var client = tgCacheService.getClientOrThrow(phone);

        var chatClient = new ChatClient(client.getClient());

        var latch = chatClient.startRequest();
        chatClient.getChat(chatId);
        latch.await();

        var chatDataClient = new ChatDataClient(chatClient.getClient());
        var user = userRepository.findById(phone).orElseThrow();
        var info = user.getChatIdToInfoStats().getOrDefault(chatId, new InfoStats());

        chatDataClient.updateChatData(chatId, info);

        userService.updateStats(chatDataClient.getStats(), phone, chatId);
    }

    private void loadChatPhotos(ChatClient chatClient) throws InterruptedException {
        chatClient.loadChatImages();
    }
}

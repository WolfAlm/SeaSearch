package space.seasearch.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import space.seasearch.spring.dto.ChatDto;
import space.seasearch.spring.exception.SeaSearchClientNotFoundException;
import space.seasearch.spring.mapper.ChatMapper;
import space.seasearch.telegram.client.ChatClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TgChatService {

    private final TGCacheService tgCacheService;
    private final ChatMapper chatMapper;

    public List<ChatDto> getDialogues(String userPhone) throws SeaSearchClientNotFoundException, InterruptedException {
        var client = tgCacheService.getClientOrThrow(userPhone);

        var chatClient = new ChatClient(client.getClient());
        var latch = chatClient.startRequest();
        chatClient.getAllChats();
        latch.await();

        var chats = chatClient.getIds();

        chats.forEach(id -> {
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

    private void loadChatPhotos(ChatClient chatClient) throws InterruptedException {
        chatClient.loadChatImages();
    }
}

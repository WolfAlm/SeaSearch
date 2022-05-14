package space.seasearch.telegram.client;

import it.tdlight.common.ResultHandler;
import it.tdlight.jni.TdApi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;


public class ChatClient extends TelegramClient {

    @Getter
    private List<Long> ids = new ArrayList<>();

    @Getter
    private Map<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();

    public ChatClient(it.tdlight.common.TelegramClient client) {
        super(client);
    }

    public void getAllChats() {
        client.send(new TdApi.GetChats(new TdApi.ChatListMain(), 10), new ChatHandler());
    }

    public void getChat(Long id) {
        client.send(new TdApi.GetChat(id), new ChatHandler());
    }

    public void loadChatImages() throws InterruptedException {
        var viableChats = chats.values().stream().map(c -> c.photo).filter(Objects::nonNull).toList();
        if (viableChats.isEmpty()) {
            return;
        }
        this.countDownLatch = new CountDownLatch(viableChats.size());
        chats.values().stream()
                .filter(chat -> chat.photo != null)
                .forEach(chat ->
                        client.send(new TdApi.DownloadFile(chat.photo.small.id, 1, 0, 0, true), new OnImageDownloaded(chat.id))
                );
        this.countDownLatch.await();
    }

    private void onChatSuccess(TdApi.Chats chats) {
        Arrays.stream(chats.chatIds).forEach(id -> ids.add(id));
        countDownLatch.countDown();
    }


    private class ChatHandler implements ResultHandler {

        @Override
        public void onResult(TdApi.Object object) {
            if (object.getConstructor() == TdApi.Error.CONSTRUCTOR) {
                currentError = ((TdApi.Error) object).message;
                status = ((TdApi.Error) object).code;
                countDownLatch.countDown();
            }
            if (object.getConstructor() == TdApi.Chats.CONSTRUCTOR) {
                onChatSuccess((TdApi.Chats) object);
            }
            if (object.getConstructor() == TdApi.Chat.CONSTRUCTOR) {
                onGetChat((TdApi.Chat) object);
            }

            countDownLatch.countDown();

        }
    }

    @AllArgsConstructor
    private class OnImageDownloaded implements ResultHandler {

        private long chatId;

        @Override
        public void onResult(TdApi.Object object) {
            if (object.getConstructor() == TdApi.Error.CONSTRUCTOR) {
                currentError = ((TdApi.Error) object).message;
                status = ((TdApi.Error) object).code;
                countDownLatch.countDown();
            }
            if (object.getConstructor() == TdApi.File.CONSTRUCTOR) {
                System.out.println("Downloading image");
                chats.get(chatId).photo.small = (TdApi.File) object;
                countDownLatch.countDown();
            }
        }
    }

    private void onGetChat(TdApi.Chat chat) {
        chats.put(chat.id, chat);
        countDownLatch.countDown();
    }


}

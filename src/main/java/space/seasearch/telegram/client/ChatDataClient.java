package space.seasearch.telegram.client;

import it.tdlight.common.ResultHandler;
import it.tdlight.jni.TdApi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import space.seasearch.telegram.stats.info.InfoStats;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;

public class ChatDataClient extends TelegramClient {

    private boolean isDoneLoading = false;
    private TdApi.Message lastMessageLoaded = null;
    private int mostRecentMessageDate = 0;
    private long lastMessageId;

    @Setter
    @Getter
    private InfoStats stats = new InfoStats();


    public ChatDataClient(it.tdlight.common.TelegramClient client) {
        super(client);
    }

    public void extractData(long chatId, long lastMessageId) throws InterruptedException {
        this.lastMessageId = lastMessageId;
        while (!isDoneLoading) {
            this.countDownLatch = new CountDownLatch(1);
            client.send(new TdApi.GetChatHistory(chatId, this.lastMessageId, 0, 99, false), new MessageHandler());
            this.countDownLatch.await();
        }
        this.stats.updateLastTimeLoaded();
    }

    public void updateChatData(long chatId, InfoStats infoStats) throws InterruptedException {
        var lastloaded = stats.getLastLoadInstant();
        while (!isDoneLoading) {
            this.countDownLatch = new CountDownLatch(1);
            client.send(new TdApi.GetChatHistory(chatId, this.lastMessageId, 0, 99, false), new MessageHandler(lastloaded));
            this.countDownLatch.await();
        }
        this.stats.updateLastTimeLoaded();
    }


    @AllArgsConstructor
    @NoArgsConstructor
    private class MessageHandler implements ResultHandler {

        private Instant updated = null;

        @Override
        public void onResult(TdApi.Object object) {
            if (object.getConstructor() == TdApi.Messages.CONSTRUCTOR) {
                TdApi.Message[] messages = ((TdApi.Messages) object).messages;
                int chunkSize = messages.length;

                if (chunkSize == 0) {
                    isDoneLoading = true;
                } else {
                    lastMessageLoaded = messages[chunkSize - 1];
                    mostRecentMessageDate = Math.max(mostRecentMessageDate, messages[0].date);

                    for (var message : messages) {
                        if (updated != null && updated.toEpochMilli() > message.date) {
                            isDoneLoading = true;
                            break;
                        }
                        stats.incrementMessageCount(message);
                        stats.messageDailyStatUpdate(message);
                        stats.updateOldestMessagedate(message);

                        if (message.content.getConstructor() == TdApi.MessageText.CONSTRUCTOR)
                            stats.countWrodsAndSymbols(message);
                    }
                }
                lastMessageId = lastMessageLoaded == null ? 0 : lastMessageLoaded.id;
                countDownLatch.countDown();
            } else {
                System.out.println("Message Handler... :" + object.getConstructor() + object);
                countDownLatch.countDown();
            }
        }
    }
}

package space.seasearch.telegram.client;

import it.tdlight.common.ResultHandler;
import it.tdlight.jni.TdApi;
import lombok.Setter;
import space.seasearch.telegram.communication.message.UtilMessage;
import space.seasearch.telegram.stats.info.InfoStats;

import java.util.Map;

public class ChatDataClient extends TelegramClient {

    private boolean isDoneLoading = false;
    private TdApi.Message lastMessageLoaded = null;
    private int mostRecentMessageDate;

    @Setter
    private InfoStats stats;


    protected ChatDataClient(it.tdlight.common.TelegramClient client) {
        super(client);
    }

    public void extractData(long chatId, long lastMessageId) {
        while(!isDoneLoading){
            long fromMessageId = 0;

//            if (lastMessage != null) {
//                fromMessageId = lastMessage.id;
//            }
        }
        client.send(new TdApi.GetChatHistory(chatId, lastMessageId, 0, 99, false), new MessageHandler());
    }

    private class MessageHandler implements ResultHandler {

        @Override
        public void onResult(TdApi.Object object) {
            if (object.getConstructor() == TdApi.Messages.CONSTRUCTOR) {
                TdApi.Message[] messages = ((TdApi.Messages) object).messages;
                int chunkSzie = messages.length;

                if (chunkSzie == 0) {
                    isDoneLoading = true;
                } else {
                    lastMessageLoaded = messages[chunkSzie - 1];
                    mostRecentMessageDate = Math.max(mostRecentMessageDate, messages[0].date);

                    for (var message : messages) {
                        if (message.date <= mostRecentMessageDate) {
                            isDoneLoading = true;
                            break;
                        }
                        stats.incrementMessageCount(message);
                        stats.countWrodsAndSymbols(message);
                    }
                }

//                parseMessage();
            } else {
                System.out.println("Message Handler... :" + object.getConstructor() + object);
            }
        }
    }


    private void parseMessageType(TdApi.Message message) {
        switch (message.content.getConstructor()) {
            case TdApi.MessageVoiceNote.CONSTRUCTOR:
                if (message.isOutgoing) {
                    stats.setOutgoingAudio(stats.getOutgoingAudio() + 1);
                } else {
                    stats.setIncomingAudio(stats.getIncomingAudio() + 1);
                }
                break;
            case TdApi.MessageDocument.CONSTRUCTOR:
                if (message.isOutgoing) {
                    stats.setOutgoingDocument(stats.getOutgoingDocument() + 1);
                } else {
                    stats.setIncomingDocument(stats.getIncomingDocument() + 1);
                }
                break;
            case TdApi.MessagePhoto.CONSTRUCTOR:
                if (message.isOutgoing) {
                    stats.setOutgoingPhoto(stats.getOutgoingPhoto() + 1);
                } else {
                    stats.setIncomingPhoto(stats.getIncomingPhoto() + 1);
                }
                break;
            case TdApi.MessageSticker.CONSTRUCTOR:
                if (message.isOutgoing) {
                    stats.setOutgoingSticker(stats.getOutgoingSticker() + 1);
                } else {
                    stats.setIncomingSticker(stats.getIncomingSticker() + 1);
                }
                break;
            case TdApi.MessageText.CONSTRUCTOR:
                // Считаем количество символов.
                long symbol = ((TdApi.MessageText) message.content).text.text.length();
                // Получаем список слов по разделителям.
                String[] words = ((TdApi.MessageText) message.content).text.text
                        .split("[,;:\\[\\]()+.\\\\!?\\s]+");
                // Заносим слова в словарь.
                Map<String, Integer> wordsDictionary = stats.getDictionaryWords();
                // Считаем количество слов.
                long countWords = 0;

                for (String word : words) {
                    // Проверяем, что слово содержит только буквы.
                    if (!UtilMessage.deleteNotLetters(word).equals("")) {
                        countWords += 1;

                        wordsDictionary.merge(word.toLowerCase(), 1, Integer::sum);
                    }
                }

                if (message.isOutgoing) {
                    stats.setOutgoingSymbol(stats.getOutgoingSymbol() + symbol);
                    stats.setOutgoingWord(stats.getOutgoingWord() + countWords);
                } else {
                    stats.setIncomingSymbols(stats.getIncomingSymbols() + symbol);
                    stats.setIncomingWords(stats.getIncomingWords() + countWords);
                }
                break;
            case TdApi.MessageVideo.CONSTRUCTOR:
                if (message.isOutgoing) {
                    stats.setOutgoingVideo(stats.getOutgoingVideo() + 1);
                } else {
                    stats.setIncomingVideo(stats.getIncomingVideo() + 1);
                }
                break;
        }
    }
}

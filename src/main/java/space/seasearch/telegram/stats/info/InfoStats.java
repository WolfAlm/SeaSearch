package space.seasearch.telegram.stats.info;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.tdlight.jni.TdApi;
import lombok.Data;
import space.seasearch.telegram.communication.message.UtilMessage;

import java.util.*;

@Data
public class InfoStats {

    private static ObjectMapper mapper = new ObjectMapper();
    private boolean isUpdated;
    private int allMessage;
    private int outgoingMessage;
    private int incomingMessage;
    private long allWord;
    private long outgoingWord;
    private long incomingWords;
    private long allSymbol;
    private long outgoingSymbol;
    private long incomingSymbols;
    private int allAudio;
    private int outgoingAudio;
    private int incomingAudio;
    private int allDocument;
    private int outgoingDocument;
    private int incomingDocument;
    private int allPhoto;
    private int outgoingPhoto;
    private int incomingPhoto;
    private int allSticker;
    private int outgoingSticker;
    private int incomingSticker;
    private int allVideo;
    private int outgoingVideo;
    private int incomingVideo;
    private int allForward;
    private int outgoingForward;
    private int incomingForward;
    /**
     * Количество общительных дней.
     */
    private int countDaysMessage;
    /**
     * Количество сообщений в пиковый день общения.
     */
    private int countMaxMessage;
    private String dateFirstMessage;
    private int countAverageMessage;
    private Map<String, Integer> dictionaryWords = new HashMap<>();
    private List<List<Object>> words;
    /**
     * Список с информацией о количестве смешанных сообщений в общительные дни.
     */
    private List<MessagesPerDay> messagesAllOfDay = new ArrayList<>();
    /**
     * Список с информацией о количестве исходящих сообщений в общительные дни.
     */
    private List<MessagesPerDay> messagesOutgoingOfDay = new ArrayList<>();
    /**
     * Список с информацией о количестве входящих сообщений в общительные дни.
     */
    private List<MessagesPerDay> messagesIncomingOfDay = new ArrayList<>();

    /**
     * Сериализует все три списки сообщений, с информацией о количестве сообщений по дням, в JSON вид
     * для передачи данных на фронт для отрисовки графики.
     *
     * @return JSON представление трех списков.
     */
    public String jsonMessages() {
        try {
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(List.of(messagesAllOfDay, messagesIncomingOfDay,
                            messagesOutgoingOfDay));
        } catch (JsonProcessingException a) {
            return "[[],[],[]]";
        }
    }

    /**
     * Сериализирует словарь 100 популярных слов с их количеством в JSON представление для облака
     * слов.
     *
     * @return JSON представление 100 популярных слов.
     */
    public String jsonWords() {
        if (words == null) {
            words = new ArrayList<>();

            int step = 0;
            for (var a : dictionaryWords.entrySet()) {
                if (a.getKey().length() > 1 && !UtilMessage.EXTRA_WORDS.contains(a.getKey())) {
                    List<Object> b = List.of(a.getKey(), a.getValue());
                    words.add(b);

                    if (++step == 100) {
                        break;
                    }
                }
            }

            dictionaryWords = new HashMap<>();
        }

        try {
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(words);
        } catch (JsonProcessingException a) {
            return "[]";
        }
    }

    public void countWrodsAndSymbols(TdApi.Message message) {
        long symbol = ((TdApi.MessageText) message.content).text.text.length();
        String[] words = ((TdApi.MessageText) message.content).text.text
                .split("[,;:\\[\\]()+.\\\\!?\\s]+");

        Arrays.stream(words)
                .filter(word -> !UtilMessage.deleteNotLetters(word).equals(""))
                .forEach(word -> this.dictionaryWords.merge(word.toLowerCase(), 1, Integer::sum));
    }

    public void incrementMessageCount(TdApi.Message message) {

        if (message.forwardInfo != null) {
            if (message.isOutgoing) {
                this.outgoingForward++;
            } else {
                this.incomingForward++;
            }

            return;
        }

        switch (message.content.getConstructor()) {
            case TdApi.MessageVoiceNote.CONSTRUCTOR:
                if (message.isOutgoing) {
                    this.outgoingAudio++;
                } else {
                    this.incomingAudio++;
                }
                break;
            case TdApi.MessageDocument.CONSTRUCTOR:
                if (message.isOutgoing) {
                    this.outgoingDocument++;
                } else {
                    this.incomingDocument++;
                }
                break;
            case TdApi.MessagePhoto.CONSTRUCTOR:
                if (message.isOutgoing) {
                    this.outgoingPhoto++;
                } else {
                    this.incomingPhoto++;
                }
                break;
            case TdApi.MessageSticker.CONSTRUCTOR:
                if (message.isOutgoing) {
                    this.outgoingSticker++;
                } else {
                    this.incomingSticker++;
                }
                break;
            case TdApi.MessageText.CONSTRUCTOR:
                long symbols = ((TdApi.MessageText) message.content).text.text.length();
                String[] words = ((TdApi.MessageText) message.content).text.text
                        .split("[,;:\\[\\]()+.\\\\!?\\s]+");

                long wordsCount = Arrays.stream(words).filter(word -> !UtilMessage.deleteNotLetters(word).equals("")).count();

                if (message.isOutgoing) {
                    this.outgoingSymbol += symbols;
                    this.outgoingWord += wordsCount;
                } else {
                    this.incomingSymbols += symbols;
                    this.incomingWords += wordsCount;
                }
                break;
            case TdApi.MessageVideo.CONSTRUCTOR:
                if (message.isOutgoing) {
                    this.outgoingVideo++;
                } else {
                    this.incomingVideo++;
                }
                break;
        }
    }
}

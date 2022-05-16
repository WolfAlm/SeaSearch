package space.seasearch.telegram.stats.info;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.tdlight.jni.TdApi;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import space.seasearch.telegram.communication.message.UtilMessage;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Data
@Getter
public class InfoStats {

    private static ObjectMapper mapper = new ObjectMapper();

    private int outgoingMessage = 0;
    private int incomingMessage = 0;

    private long outgoingWord = 0;
    private long incomingWords = 0;

    private long outgoingSymbol = 0;
    private long incomingSymbols = 0;

    private int outgoingAudio = 0;
    private int incomingAudio = 0;

    private int outgoingDocument = 0;
    private int incomingDocument = 0;

    private int outgoingPhoto = 0;
    private int incomingPhoto = 0;

    private int outgoingSticker = 0;
    private int incomingSticker = 0;

    private int outgoingVideo = 0;
    private int incomingVideo = 0;

    private int outgoingForward = 0;
    private int incomingForward = 0;

    private int countDaysMessage = 0;

    private int countMaxMessage = 0;

    @Getter
    private Instant lastLoadInstant;
    @Setter
    private String dateFirstMessage;
    private int countAverageMessage;
    private Map<String, Integer> dictionaryWords = new TreeMap<>();
    private List<List<Object>> words;

    private List<MessagesPerDay> messagesPerActiveDay = new ArrayList<>();

    private List<MessagesPerDay> messagesOutgoingOfDay = new ArrayList<>();

    private List<MessagesPerDay> messagesIncomingOfDay = new ArrayList<>();

    @Setter
    private Map<LocalDate, Integer> dateToDailyMessageCount = new HashMap<>();
    @Setter
    private Map<LocalDate, Integer> dateToMessageCountIncoming = new HashMap<>();
    @Getter
    @Setter
    private Map<LocalDate, Integer> dateToMessageCountOutgoing = new HashMap<>();

    /**
     * Сериализует все три списки сообщений, с информацией о количестве сообщений по дням, в JSON вид
     * для передачи данных на фронт для отрисовки графики.
     *
     * @return JSON представление трех списков.
     */
    public String jsonMessages() {
        try {
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(List.of(messagesPerActiveDay, messagesIncomingOfDay,
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

    public void updateLastTimeLoaded() {
        lastLoadInstant = Instant.now();
    }

    public void countWrodsAndSymbols(TdApi.Message message) {
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

        Arrays.stream(words)
                .filter(word -> !UtilMessage.deleteNotLetters(word).equals(""))
                .forEach(word -> this.dictionaryWords.merge(word.toLowerCase(), 1, Integer::sum));
    }


    public void updateOldestMessagedate(TdApi.Message message) {
        this.dateFirstMessage = message == null ? dateFirstMessage : String.valueOf(message.date);
    }

    public void messageDailyStatUpdate(TdApi.Message message) {
        this.dateToDailyMessageCount.merge(new Date(message.date * 1000L).toInstant().atZone(
                ZoneId.of("GMT+3")).toLocalDate(), 1, Integer::sum);

        if (message.isOutgoing) {
            dateToMessageCountOutgoing.merge(new Date(message.date * 1000L).toInstant().atZone(
                    ZoneId.of("GMT+3")).toLocalDate(), 1, Integer::sum);
        } else {
            dateToMessageCountIncoming.merge(new Date(message.date * 1000L).toInstant().atZone(
                    ZoneId.of("GMT+3")).toLocalDate(), 1, Integer::sum);
        }
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
                if (message.isOutgoing) {
                    this.outgoingMessage++;
                } else {
                    this.incomingMessage++;
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

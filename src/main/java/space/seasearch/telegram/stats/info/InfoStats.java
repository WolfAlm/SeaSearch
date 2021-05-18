package space.seasearch.telegram.stats.info;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import space.seasearch.telegram.communication.message.UtilMessage;

@Data
public class InfoStats {

  private static ObjectMapper mapper = new ObjectMapper();
  private boolean isUpdated;
  private long allMessage;
  private long outgoingMessage;
  private long incomingMessage;
  private long allWord;
  private long outgoingWord;
  private long incomingWord;
  private long allSymbol;
  private long outgoingSymbol;
  private long incomingSymbol;
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
  private Map<String, Integer> dictionaryWords;
  private List<List<Object>> words;
  /**
   * Список с информацией о количестве смешанных сообщений в общительные дни.
   */
  private List<MessagesPerDay> messagesAllOfDay;
  /**
   * Список с информацией о количестве исходящих сообщений в общительные дни.
   */
  private List<MessagesPerDay> messagesOutgoingOfDay;
  /**
   * Список с информацией о количестве входящих сообщений в общительные дни.
   */
  private List<MessagesPerDay> messagesIncomingOfDay;

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
        if (!UtilMessage.EXTRA_WORDS.contains(a.getKey())) {
          List<Object> b = List.of(a.getKey(), a.getValue());
          words.add(b);

          if (++step == 100) {
            break;
          }
        }
      }

      dictionaryWords = null;
    }

    try {
      return mapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(words);
    } catch (JsonProcessingException a) {
      return "[]";
    }
  }
}

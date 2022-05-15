package space.seasearch.telegram.stats.profile;

import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Data;
import space.seasearch.telegram.communication.message.Message;
import space.seasearch.telegram.stats.info.InfoStats;
import space.seasearch.telegram.stats.info.MessagesPerDay;

@Data
public class ProfileStats {

  private final static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
  private String name;
  private String nickname;
  private String photoPath;
  private InfoStats infoStats = new InfoStats();
  private final TelegramClient client;
  private Message message;

  public ProfileStats(TelegramClient client) {
    this.client = client;
  }

  public void updateInfo() {
    if (!infoStats.isUpdated()) {
      infoStats.setUpdated(true);

      infoStats.setAllWord(infoStats.getIncomingWords() + infoStats.getOutgoingWord());
      infoStats.setAllSymbol(infoStats.getIncomingSymbols() + infoStats.getOutgoingSymbol());
      infoStats.setAllAudio(infoStats.getIncomingAudio() + infoStats.getOutgoingAudio());
      infoStats.setAllSticker(infoStats.getIncomingSticker() + infoStats.getOutgoingSticker());
      infoStats.setAllPhoto(infoStats.getIncomingPhoto() + infoStats.getOutgoingPhoto());
      infoStats.setAllSymbol(infoStats.getIncomingSymbols() + infoStats.getOutgoingSymbol());
      infoStats.setAllVideo(infoStats.getIncomingVideo() + infoStats.getOutgoingVideo());
      infoStats.setAllDocument(infoStats.getIncomingDocument() + infoStats.getOutgoingDocument());
      infoStats.setAllForward(infoStats.getIncomingForward() + infoStats.getOutgoingForward());
      infoStats.setAllMessage(infoStats.getOutgoingMessage() + infoStats.getIncomingMessage());

      // Сообщения по дням.
      Map<LocalDate, Integer> messagesOfDay = message.getMessagesOfDay();

      infoStats.setMessagesAllOfDay(createMessagesPerDay(messagesOfDay));
      infoStats.setMessagesIncomingOfDay(createMessagesPerDay(message.getMessagesIncomingOfDay()));
      infoStats.setMessagesOutgoingOfDay(createMessagesPerDay(message.getMessagesOutgoingOfDay()));

      // Среднее количество сообщений в день.
      LongSummaryStatistics statistics =
              messagesOfDay.values().parallelStream().mapToLong(a -> a).summaryStatistics();

      infoStats.setCountAverageMessage((int) statistics.getAverage());
      // Количество общительных дней
      infoStats.setCountDaysMessage((int) statistics.getCount());

      // День с наибольшим количеством дней
      infoStats.setCountMaxMessage((int) statistics.getMax());

      // Первое сообщение.
      TdApi.Message firstMessage = message.getLastMessage();
      if (firstMessage != null && infoStats.getDateFirstMessage() == null) {
        infoStats.setDateFirstMessage(dateFormat.format(new Date(firstMessage.date * 1000L)));
      }

      // Слова
      infoStats.setDictionaryWords(infoStats.getDictionaryWords().entrySet()
              .stream()
              .sorted(Entry.<String, Integer>comparingByValue().reversed())
              .collect(Collectors.toMap(
                      Map.Entry::getKey,
                      Map.Entry::getValue,
                      (oldValue, newValue) -> oldValue, LinkedHashMap::new)));

      // Очистим все данные.
      message.setMessagesOfDay(new HashMap<>());
      message.setMessagesIncomingOfDay(new HashMap<>());
      message.setMessagesOutgoingOfDay(new HashMap<>());
    }
  }

  private List<MessagesPerDay> createMessagesPerDay(Map<LocalDate, Integer> messageDictionary) {
    List<MessagesPerDay> messagesPerDay = new ArrayList<>();

    for (var a : messageDictionary.entrySet()) {
      messagesPerDay.add(new MessagesPerDay(a.getKey(), a.getValue()));
    }

    messagesPerDay.sort(Comparator.comparing(MessagesPerDay::getDate));

    return messagesPerDay;
  }

  /**
   * Запускает процесс парсинга сообщений.
   */
  public void parseMessage(int newestSavedMessageDate) {
    message.setStats(infoStats);
    message.startParseMessage(newestSavedMessageDate);
  }

  public void restartMessage(int newestSavedMessageDate) {
    message.setStartParse(false);
    infoStats.setUpdated(false);
    parseMessage(newestSavedMessageDate);
  }

  /**
   * Проверка, получены ли все сообщения или нет.
   *
   * @return Результат готовности.
   */
  public boolean isHaveAllMessage() {
    return message.isHaveFullMessages();
  }

  /**
   * Получает количество полученных сообщений в процессе парсинга.
   *
   * @return Количество полученных сообщений.
   */
  public int getCountAllMessage() {
    return infoStats.getIncomingMessage() + infoStats.getOutgoingMessage();
  }

  /**
   * Получает в строковом представлении информацию о дате последнего сообщения, которое было
   * получено в процессе парсинга.
   *
   * @return Дата последнего сообщения в строковом представлении.
   */
  public String getDateLastMessage() {
    if (message.getLastMessage() != null) {
      return dateFormat.format(new Date(message.getLastMessage().date * 1000L));
    } else {
      return dateFormat.format(LocalDate.of(1970, 1, 1));
    }
  }
}

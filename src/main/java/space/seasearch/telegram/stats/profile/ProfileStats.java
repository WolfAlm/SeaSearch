package space.seasearch.telegram.stats.profile;

import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
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
  private final InfoStats infoStats = new InfoStats();
  private final TelegramClient client;
  private Message message;

  public ProfileStats(TelegramClient client) {
    this.client = client;
  }

  public void updateInfo() {
    if (!infoStats.isUpdated()) {
      infoStats.setUpdated(true);
      infoStats.setDictionaryWords(new HashMap<>());
      message.makeMessage(infoStats);

      infoStats.setAllWord(infoStats.getIncomingWord() + infoStats.getOutgoingWord());
      infoStats.setAllSymbol(infoStats.getIncomingSymbol() + infoStats.getOutgoingSymbol());
      infoStats.setAllAudio(infoStats.getIncomingAudio() + infoStats.getOutgoingAudio());
      infoStats.setAllSticker(infoStats.getIncomingSticker() + infoStats.getOutgoingSticker());
      infoStats.setAllPhoto(infoStats.getIncomingPhoto() + infoStats.getOutgoingPhoto());
      infoStats.setAllSymbol(infoStats.getIncomingSymbol() + infoStats.getOutgoingSymbol());
      infoStats.setAllVideo(infoStats.getIncomingVideo() + infoStats.getOutgoingVideo());
      infoStats.setAllDocument(infoStats.getIncomingDocument() + infoStats.getOutgoingDocument());
      infoStats.setAllForward(infoStats.getIncomingForward() + infoStats.getOutgoingForward());

      infoStats.setAllMessage(message.getIncomingMessages().size() +
          message.getOutgoingMessages().size());
      infoStats.setIncomingMessage(message.getIncomingMessages().size());
      infoStats.setOutgoingMessage(message.getOutgoingMessages().size());
      // Сообщения по дням.
      Map<LocalDate, Long> messagesOfDay = message.getMessages().parallelStream()
          .collect(Collectors.groupingBy(e -> (new Date(e.date * 1000L).toInstant().atZone(
              ZoneId.of("GMT+3")).toLocalDate()), Collectors.counting()));

      infoStats.setMessagesAllOfDay(createMessagesPerDay(messagesOfDay));
      infoStats.setMessagesIncomingOfDay(createMessagesPerDay(message.getIncomingMessages()));
      infoStats.setMessagesOutgoingOfDay(createMessagesPerDay(message.getOutgoingMessages()));

      // Среднее количество сообщений в день.
      LongSummaryStatistics statistics =
          messagesOfDay.values().parallelStream().mapToLong(a -> a).summaryStatistics();

      infoStats.setCountAverageMessage((int) statistics.getAverage());
      // Количество общительных дней
      infoStats.setCountDaysMessage((int) statistics.getCount());

      // День с наибольшим количеством дней
      infoStats.setCountMaxMessage((int) statistics.getMax());

      // Первое сообщение.
      TdApi.Message firstMessage = message.getMessages().get(getMessage().getMessages().size() - 1);
      infoStats.setDateFirstMessage(dateFormat.format(new Date(firstMessage.date * 1000L)));

      // Слова
      infoStats.setDictionaryWords(infoStats.getDictionaryWords().entrySet()
          .stream()
          .sorted(Entry.<String, Integer>comparingByValue().reversed())
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              Map.Entry::getValue,
              (oldValue, newValue) -> oldValue, LinkedHashMap::new)));

      // Очистим все данные.
      message.setMessages(new ArrayList<>());
      message.setIncomingMessages(new ArrayList<>());
      message.setOutgoingMessages(new ArrayList<>());
    }
  }

  private List<MessagesPerDay> createMessagesPerDay(List<TdApi.Message> messageList) {
    return createMessagesPerDay(messageList.parallelStream()
        .collect(Collectors.groupingBy(e -> (new Date(e.date * 1000L).toInstant().atZone(
            ZoneId.of("GMT+3")).toLocalDate()), Collectors.counting())));
  }

  private List<MessagesPerDay> createMessagesPerDay(Map<LocalDate, Long> messageDictionary) {
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
  public void parseMessage() {
    message.startParseMessage();
  }

  public void restartMessage() {
    message.setStartParse(false);
    parseMessage();
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
    return message.getCountMessage();
  }

  /**
   * Получает в строковом представлении информацию о дате последнего сообщения, которое было
   * получено в процессе парсинга.
   *
   * @return Дата последнего сообщения в строковом представлении.
   */
  public String getDateLastMessage() {
    return dateFormat.format(new Date(message.getLastMessage().date * 1000L));
  }
}

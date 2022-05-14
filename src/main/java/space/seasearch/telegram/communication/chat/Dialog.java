package space.seasearch.telegram.communication.chat;

import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.Chat;
import it.tdlight.jni.TdApi.ChatTypeBasicGroup;
import it.tdlight.jni.TdApi.ChatTypePrivate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import space.seasearch.telegram.communication.message.Message;
import space.seasearch.telegram.photo.Photo;
import space.seasearch.telegram.stats.profile.GroupStats;
import space.seasearch.telegram.stats.profile.ProfileStats;
import space.seasearch.telegram.stats.profile.UserStats;
import space.seasearch.telegram.user.UserProfile;

public class Dialog {
  @Getter
  private ParserDialog parserDialog;
  /**
   * Строка, введенная пользователем для поиска среди всех диалогов и фильтрации.
   */
  @Setter
  private String search = "";
  @Getter
  private UserProfile userProfile;
  /**
   * Соединение с сервером Telegram для отправки запросов и получения данных.
   */
  private final TelegramClient client;

  /**
   * Запустить процесс получения диалогов, если он не был запущен до этого. После запуска меняется
   * состояние.
   */
  private boolean startParse = true;
  /**
   * Упорядоченные диалоги со всеми типами: и групповые, и личные.
   */
  private final Map<Long, Chat> orderedChats = new LinkedHashMap<>();
  /**
   * Упорядоченные личные диалоги.
   */
  private final Map<Long, Chat> privateChats = new LinkedHashMap<>();
  /**
   * Упорядоченные групповые диалоги.
   */
  private final Map<Long, Chat> groupChats = new LinkedHashMap<>();
  @Getter
  private final Map<Long, ProfileStats> profileStats = new HashMap<>();
  private Semaphore semaphore;

  public Dialog(TelegramClient client) {
    this.semaphore = new Semaphore(1);
    this.client = client;
    parserDialog = new ParserDialog(client, semaphore);
  }

  /**
   * Запустить процесс получения диалогов и фотографий для них, если не был запущен.
   */
  public void startParse() {
    if (startParse) {
      startParse = false;

      new Thread(parserDialog).start();

      try {
        semaphore.acquire();
      } catch (InterruptedException ignored) {
      }

      makeChats();

      downloadPhoto();
    }
  }

  /**
   * Скачивает фотографии для диалогов.
   */
  public void downloadPhoto() {
    // Запустим счетчик фотографий, сколько должно быть скачано.
    CountDownLatch countDownLatch = new CountDownLatch(orderedChats.size());
    Photo photo = new Photo(client, countDownLatch);
    photo.downloadPhoto(orderedChats);
    // Ждем, пока скачаются все фотографии чатов, а затем уже скачиваем фото пользователя.
    try {
      countDownLatch.await();
      countDownLatch = new CountDownLatch(1);

      userProfile = new UserProfile(client, countDownLatch);
      userProfile.parseUser();
      countDownLatch.await();
    } catch (InterruptedException ignored) {
    }

    countDownLatch = new CountDownLatch(1);
    photo = new Photo(client, countDownLatch);
    photo.downloadPhoto(userProfile.getUser());
    try {
      countDownLatch.await();
    } catch (InterruptedException ignored) {
    }
    // ОБновляем путь для пользователя.
    userProfile.setPhotoPath(photo.parsePath(userProfile.getUser()));

    // Добавляем все пути к изображениям для каждого пользователя.
    for (Chat chat : orderedChats.values()) {
      profileStats.get(chat.id).setPhotoPath(photo.parsePath(chat));
    }
  }

  /**
   * Создает упорядоченный список чатов всех типов: смешанных, личных и групповых.
   */
  private void makeChats() {
    CountDownLatch countDownLatch;
    NavigableSet<PositionDialog> positionDialogs = parserDialog.getPositionDialogs();
    ConcurrentMap<Long, TdApi.Chat> chats = parserDialog.getChats();

    for (PositionDialog position : positionDialogs) {
      Long chatId = position.getChatId();
      Chat chat = chats.get(chatId);

      // В зависимости от типа чата определяем, какого профиля нужно создавать,
      // не добавляем каналы, мертвые аккаунты.
      switch (chat.type.getConstructor()) {
        case ChatTypePrivate.CONSTRUCTOR:
          countDownLatch = new CountDownLatch(1);
          UserStats userStats = new UserStats(client, new Message(client, chatId),
              countDownLatch);
          userStats.parseUser(((ChatTypePrivate) chat.type).userId);

          try {
            countDownLatch.await();
          } catch (InterruptedException ignored) {
          }

          if (!userStats.isDeleted()) {
            profileStats.put(chatId, userStats);
            privateChats.put(chatId, chat);
            orderedChats.put(chatId, chat);
          }
          break;
        case ChatTypeBasicGroup.CONSTRUCTOR:
          GroupStats groupStats = new GroupStats(client, new Message(client, chatId), chat.title);

          groupStats.parseGroup(((ChatTypeBasicGroup) chat.type).basicGroupId);

          orderedChats.put(chatId, chat);
          groupChats.put(chatId, chat);
          profileStats.put(chatId, groupStats);
          break;
      }
    }

    parserDialog = null;
  }

  /**
   * Фильтрация диалогов по введенной строке пользователем среди переданного диалога.
   *
   * @param chats Диалог, которого нужно отфильтровать.
   * @return Результат фильтрации.
   */
  private Map<Long, Chat> searchChats(Map<Long, Chat> chats) {
    if (search.equals("")) {
      return chats;
    }

    return chats
        .entrySet().parallelStream()
        .filter(a -> a.getValue().title.toLowerCase().contains(search.toLowerCase()))
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }

  public Map<Long, Chat> getPrivateChats() {
    return searchChats(privateChats);
  }

  public Map<Long, Chat> getOrderChats() {
    return searchChats(orderedChats);
  }

  public Map<Long, Chat> getGroupChats() {
    return searchChats(groupChats);
  }
}
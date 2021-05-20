package space.seasearch.telegram.communication.chat;

import it.tdlight.common.ResultHandler;
import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.Chat;
import it.tdlight.jni.TdApi.Error;
import it.tdlight.jni.TdApi.UpdateChatPosition;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import lombok.Getter;

public class ParserDialog implements Runnable {
  /**
   * Соединение с сервером Telegram для отправки запросов и получения данных.
   */
  private final TelegramClient client;

  /**
   * Указатель на состояние, что идет ли процесс получения диалогов в данный момент.
   */
  @Getter
  private boolean processParse = true;

  /**
   * Список чатов. (ID чата, сам чат)
   */
  @Getter
  private final ConcurrentMap<Long, Chat> chats = new ConcurrentHashMap<>();
  /**
   * Положение чата в списке диалога.
   */
  @Getter
  private final NavigableSet<PositionDialog> positionDialogs = new TreeSet<>();
  private final Semaphore semaphore;

  public ParserDialog(TelegramClient client, Semaphore semaphore) {
    this.client = client;
    this.semaphore = semaphore;

    try {
      semaphore.acquire();
    } catch (InterruptedException ignored) {
    }
  }

  /**
   * Добавляет новый чат в список диалогов.
   *
   * @param chat Чат.
   */
  public void addChat(Chat chat) {
    chats.put(chat.id, chat);
  }

  /**
   * Добавляет новую позицию чата в список позиций.
   *
   * @param chat Чат, у кого позицию нужно добавить.
   */
  public void addPosition(UpdateChatPosition chat) {
    if (chat.position.list.getConstructor() != TdApi.ChatListMain.CONSTRUCTOR) {
      return;
    }

    Chat chat1 = chats.get(chat.chatId);
    chat1.positions = new TdApi.ChatPosition[]{chat.position};

    positionDialogs.add(new PositionDialog(chat1.id, chat.position));
  }

  /**
   * Запрашивает диалоги у сервера и вытягивает всех их.
   */
  public void parseChats() {
    synchronized (positionDialogs) {
      if (processParse) {
        long offsetOrder = Long.MAX_VALUE;
        long offsetChatId = 0;

        if (!positionDialogs.isEmpty()) {
          offsetOrder = positionDialogs.last().getOrder();
          offsetChatId = positionDialogs.last().getChatId();
        }

        client.send(new TdApi.GetChats(new TdApi.ChatListMain(), offsetOrder, offsetChatId,
            30000), new ChatHandler());
      }
    }
  }

  @Override
  public void run() {
    parseChats();
  }

  private class ChatHandler implements ResultHandler {

    @Override
    public void onResult(TdApi.Object object) {
      switch (object.getConstructor()) {
        case TdApi.Chats.CONSTRUCTOR:
          // Если чатов больше нет в приходящем запросе, то мы перестаем парсить.
          if (((TdApi.Chats) object).chatIds.length == 0) {
            synchronized (positionDialogs) {
              processParse = false;
              semaphore.release();
            }
          }

          parseChats();
          break;
        case TdApi.Error.CONSTRUCTOR:
          System.err.println("Receive an error for GetChats: " + ((Error) object).message);
          break;
      }
    }

  }
}

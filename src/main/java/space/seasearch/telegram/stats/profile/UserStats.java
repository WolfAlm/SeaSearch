package space.seasearch.telegram.stats.profile;

import it.tdlight.common.ResultHandler;
import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.Object;
import it.tdlight.jni.TdApi.User;
import it.tdlight.jni.TdApi.UserTypeDeleted;
import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import space.seasearch.telegram.communication.message.Message;

public class UserStats extends ProfileStats {

  /**
   * Пользователь удален или нет.
   */
  @Getter
  private boolean isDeleted;
  /**
   * Состояние того, что получен пользователь.
   */
  private final CountDownLatch parseUser;

  public UserStats(TelegramClient client, Message message, CountDownLatch parseUser) {
    super(client);
    this.parseUser = parseUser;
    setMessage(message);
  }

  /**
   * Отправляет запрос по идентификатору пользователя для получения пользователя.
   *
   * @param userId Идентификатор пользователя, которого нужно получить.
   */
  public void parseUser(long userId) {
    getClient().send(new TdApi.GetUser(userId), new UserHandler());
  }

  private class UserHandler implements ResultHandler {

    /**
     * Из полученного объекта после запроса получает информацию, удален ли пользователь, если это не
     * так, то устанавливает ФИО и никнейм пользователя.
     *
     * @param object Пользователь, у которого нужно вытащить информацию.
     */
    @Override
    public void onResult(Object object) {
      User user = (User) object;

      if (user.type.getConstructor() == UserTypeDeleted.CONSTRUCTOR) {
        isDeleted = true;
      } else {
        setName(user.firstName + " " + user.lastName);
        setNickname(user.username);
      }

      parseUser.countDown();
    }
  }
}

package space.seasearch.telegram.stats.profile;

import it.tdlight.common.ResultHandler;
import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.BasicGroupFullInfo;
import it.tdlight.jni.TdApi.Object;
import space.seasearch.telegram.communication.message.Message;

public class GroupStats extends ProfileStats {

  public GroupStats(TelegramClient client, Message message, String nameGroup) {
    super(client);
    setName(nameGroup);
    setMessage(message);
  }

  /**
   * Отправляет запрос на получение группы по его идентификатору.
   *
   * @param groupID Идентификатор группы.
   */
  public void parseGroup(int groupID) {
    getClient().send(new TdApi.GetBasicGroupFullInfo(groupID), new GroupHandler());
  }

  private class GroupHandler implements ResultHandler {

    /**
     * Из полученного объекта после запроса получает количество участников и устанавливает это в
     * качестве информации.
     *
     * @param object Группа, у которой нужно узнать количество участников.
     */
    @Override
    public void onResult(Object object) {
      setNickname("Всего участников: " + ((BasicGroupFullInfo) object).members.length);
    }
  }
}

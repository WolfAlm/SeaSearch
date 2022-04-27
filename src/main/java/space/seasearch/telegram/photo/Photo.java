package space.seasearch.telegram.photo;

import it.tdlight.common.ResultHandler;
import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.Chat;
import it.tdlight.jni.TdApi.DownloadFile;
import it.tdlight.jni.TdApi.File;
import it.tdlight.jni.TdApi.User;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Photo {

  private final TelegramClient client;
  /**
   * Считает количество установленных фотографий.
   */
  private final CountDownLatch countDownloaded;

  /**
   * Если у чата есть фотография, то мы получаем путь расположения к этой фотографии, иначе ставим
   * мертвую фотографию.
   *
   * @param chat Чат, у которого нужно узнать путь к фотографии.
   * @return Сформированный путь к  фотографии.
   */
  public String parsePath(Chat chat) {
    if (chat.photo != null) {
      String[] folders = chat.photo.small.local.path.split(Pattern.quote(java.io.File.separator));

      return PhotoPath.PATH_TO_DATABASE + folders[folders.length - 3]
          + PhotoPath.PATH_TO_DOWNLOAD_PHOTO +
          folders[folders.length - 1];
    } else {
      return PhotoPath.PATH_TO_OWN_PHOTO;
    }
  }

  /**
   * Если у пользователя есть фотография, то мы получаем путь расположения к этой фотографии, иначе
   * ставим мертвую фотографию.
   *
   * @param user Пользователь, у которого нужно узнать путь к фотографии.
   * @return Сформированный путь к  фотографии.
   */
  public String parsePath(User user) {
    if (user.profilePhoto != null) {
      String[] folders = user.profilePhoto.small.local.path.split(Pattern.quote(java.io.File.separator));

      return PhotoPath.PATH_TO_DATABASE + folders[folders.length - 3]
          + PhotoPath.PATH_TO_DOWNLOAD_PHOTO +
          folders[folders.length - 1];
    } else {
      return PhotoPath.PATH_TO_OWN_PHOTO;
    }
  }

  /**
   * Отправляет запрос на скачку фотографии для пользователя, если у него есть фото. Иначе не
   * отправляется запрос.
   *
   * @param user Пользователь, у которого нужно отправлять запрос на скачку.
   */
  public void downloadPhoto(User user) {
    if (user.profilePhoto != null) {
      if (user.profilePhoto.small.local.path.equals("")) {
        client.send(
            new DownloadFile(user.profilePhoto.small.id, 1, 0, 0, true),
            new PhotoHandler(user));
      } else {
        countDownloaded.countDown();
      }
    } else {
      countDownloaded.countDown();
    }
  }

  /**
   * Отправляет много запросов на скачку фотографий для диалогов, если у них имеются, иначе ничего
   * не делаем.
   *
   * @param chats Диалоги, у которых нужно скачивать.
   */
  public void downloadPhoto(Map<Long, Chat> chats) {
    for (Chat chat : chats.values()) {
      if (chat.photo != null) {
        File file = chat.photo.small;

        if (file.local.path.equals("")) {
          client.send(new DownloadFile(file.id, 1, 0, 0, true),
              new PhotoHandler(chat));
        } else {
          countDownloaded.countDown();
        }
      } else {
        countDownloaded.countDown();
      }
    }
  }

  private class PhotoHandler implements ResultHandler {

    private Chat chat;
    private User user;

    public PhotoHandler(Chat chat) {
      this.chat = chat;
    }

    public PhotoHandler(User user) {
      this.user = user;
    }

    /**
     * Проверяем на полученный результат скачки. Если файл скачался, то устанавливаем этот файл в
     * свойствах объекта, если нет, то просто null.
     *
     * @param object Файл или ошибка загрузки.
     */
    @Override
    public void onResult(TdApi.Object object) {
      if (object instanceof TdApi.Error) {
        if (user != null) {
          user.profilePhoto.small = null;
        } else {
          chat.photo.small = null;
        }
      } else if (user != null) {
        user.profilePhoto.small = (File) object;
      } else if (chat != null) {
        chat.photo.small = (File) object;
      }

      countDownloaded.countDown();
    }
  }
}


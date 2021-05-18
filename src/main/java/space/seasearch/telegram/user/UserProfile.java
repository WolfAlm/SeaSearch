package space.seasearch.telegram.user;

import it.tdlight.common.ResultHandler;
import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.Object;
import it.tdlight.jni.TdApi.User;
import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.Setter;

public class UserProfile {

  @Getter
  @Setter
  private String photoPath;
  private final TelegramClient client;
  @Getter
  private User user;
  private final CountDownLatch parsed;
  public UserProfile(TelegramClient client, CountDownLatch parsed) {
    this.client = client;
    this.parsed = parsed;
  }

  public void parseUser() {
    client.send(new TdApi.GetMe(), new UserHandler());
  }

  private class UserHandler implements ResultHandler {

    @Override
    public void onResult(Object object) {
      user = (User) object;

      parsed.countDown();
    }
  }
}

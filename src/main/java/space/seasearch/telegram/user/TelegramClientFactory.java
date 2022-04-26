package space.seasearch.telegram.user;

import it.tdlight.common.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.tdlight.ClientManager;
import java.io.IOError;
import java.io.IOException;

public class TelegramClientFactory {

  public static TelegramClient createClient() {
    TelegramClient telegramClient = ClientManager.create();

    // Отвечает за подробность логов.
    telegramClient.execute(new TdApi.SetLogVerbosityLevel(0));
    // disable TDLib log
    if (telegramClient.execute(
        new TdApi.SetLogStream(
            new TdApi.LogStreamFile("1_tdlib.log", 1 << 27, false)
        )
    ) instanceof TdApi.Error) {
      throw new IOError(new IOException("Write access to the current directory is required"));
    }

    return telegramClient;
  }
}

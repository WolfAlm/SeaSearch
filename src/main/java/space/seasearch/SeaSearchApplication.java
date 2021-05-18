package space.seasearch;

import it.tdlight.common.Init;
import it.tdlight.common.TelegramClient;
import it.tdlight.common.utils.CantLoadLibrary;
import it.tdlight.jni.TdApi;
import it.tdlight.tdlib.ClientManager;
import java.io.IOError;
import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
public class SeaSearchApplication {

  static {
    // Скачка библиотеки телеграма
    try {
      Init.start();
    } catch (CantLoadLibrary cantLoadLibrary) {
      cantLoadLibrary.printStackTrace();
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(SeaSearchApplication.class, args);
  }
}

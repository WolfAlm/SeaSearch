package space.seasearch;

import it.tdlight.common.Init;
import it.tdlight.common.utils.CantLoadLibrary;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

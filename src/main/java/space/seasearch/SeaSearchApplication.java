package space.seasearch;

import it.tdlight.common.Init;
import it.tdlight.common.utils.CantLoadLibrary;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.Transactional;
import space.seasearch.spring.repository.UserRepository;

@EnableAsync
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

    @Bean
    @Transactional
    public CommandLineRunner iit(UserRepository userRepository) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                userRepository.deleteAll();
            }
        };
    }
}

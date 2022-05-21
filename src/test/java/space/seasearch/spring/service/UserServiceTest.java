package space.seasearch.spring.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import space.seasearch.spring.entity.SeaSearchUser;
import space.seasearch.spring.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class UserServiceTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void saveUser() {
        SeaSearchUser result = userRepository.save(mockUser("79253653"));
        System.out.println(result.getPhoneNumber());
    }

    @Test
    void updateGroupIds() {
    }

    @Test
    void updateStats() {
    }

    @Test
    void getInfoStats() {
    }


    private SeaSearchUser mockUser(String username) {
        SeaSearchUser user = new SeaSearchUser();
        user.setPhoneNumber(username);
        user.setChatIdToInfoStats(new HashMap<>());

        user.setLastActivity(LocalDateTime.now());

        return user;
    }
}
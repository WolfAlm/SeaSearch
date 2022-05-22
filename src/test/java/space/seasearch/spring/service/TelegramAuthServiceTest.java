package space.seasearch.spring.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import space.seasearch.spring.entity.SeaSearchUser;
import space.seasearch.spring.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class TelegramAuthServiceTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void registerUser() throws Exception {
        var userExpected = new SeaSearchUser();
        userExpected.setPhoneNumber("79");
        userExpected.setTokenPath("token");

        var userActual = userRepository.save(userExpected);

        assertEquals(userExpected, userActual);
    }

    @Test
    void logoutUser() {
        var user = new SeaSearchUser();
        user.setPhoneNumber("79");
        user.setTokenPath("token");

        user = userRepository.save(user);

        userRepository.deleteById(user.getPhoneNumber());

        var userActual = userRepository.findById(user.getPhoneNumber());
        assertFalse(userActual.isPresent());
    }
}
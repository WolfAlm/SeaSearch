package space.seasearch.spring.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import space.seasearch.spring.entity.SeaSearchUser;
import space.seasearch.spring.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

//@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@AutoConfigureDataMongo
@SpringBootTest
//@ComponentScan("space.seasearch")
class TelegramAuthServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
//    @Qualifier("telegramAuthService")
    TelegramAuthService service;

    @Test
    void processNewUser() {
    }

    @Test
    void authenticateUserWithCode() {
    }

    @Test
    void authenticateWithPassword() throws Exception {
        service.authenticateWithPassword("lol", "da");
    }

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
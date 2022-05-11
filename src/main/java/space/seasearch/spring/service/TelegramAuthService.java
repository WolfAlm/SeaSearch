package space.seasearch.spring.service;

import it.tdlight.common.TelegramClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import space.seasearch.spring.entity.UserInfo;
import space.seasearch.spring.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class TelegramAuthService {

    private final UserRepository userRepository;

    private void validateNumber(String phoneNumber) throws Exception {

    }

    public UserInfo registerUser(String phoneNumber) throws Exception {
        if (userRepository.findById(phoneNumber).isPresent())
            throw new Exception("user with phone number {} already registered");
        var user = new UserInfo();
        user.setUsername(phoneNumber);
        return userRepository.save(user);
    }

    public TelegramClient assignUserToClient(UserInfo userInfo){
        //todo
        return null;
    }
}

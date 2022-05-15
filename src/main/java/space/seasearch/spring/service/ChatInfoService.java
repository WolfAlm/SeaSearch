package space.seasearch.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import space.seasearch.spring.dto.DictWordDto;
import space.seasearch.spring.dto.GraphDto;
import space.seasearch.spring.mapper.ChatInfoMapper;
import space.seasearch.spring.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatInfoService {

    private final ChatInfoMapper mapper;
    private final UserService userService;
    private final UserRepository userRepository;


    public GraphDto getGraph(String phone, long chatId) {
        var user = userRepository.findById(phone).orElseThrow();

        return mapper.mapToGraphDto(user.getChatIdToInfoStats().get(chatId));
    }

    public List<DictWordDto> getDictionaryOfPopularWords(String phone, long chatId) {
        var user = userRepository.findById(phone).orElseThrow();

        return mapper.mapToDictionary(user.getChatIdToInfoStats().get(chatId));
    }
}

package space.seasearch.spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import space.seasearch.spring.dto.ChatDto;
import space.seasearch.spring.dto.ChatInfoDto;
import space.seasearch.spring.dto.DictWordDto;
import space.seasearch.spring.dto.GraphDto;
import space.seasearch.spring.entity.SeaSearchUser;
import space.seasearch.spring.exception.SeaSearchClientNotFoundException;
import space.seasearch.spring.repository.UserRepository;
import space.seasearch.spring.service.ChatInfoService;
import space.seasearch.spring.service.TgChatService;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final TgChatService chatService;
    private final UserRepository userRepository;
    private final ChatInfoService chatInfoService;

    @GetMapping("/chats")
    public List<ChatDto> getUserDialogues(
            @AuthenticationPrincipal SeaSearchUser user
    ) throws SeaSearchClientNotFoundException, InterruptedException {
        return chatService.getChats(user.getPhoneNumber());
    }

    @GetMapping("/chats/{chatId}/info")
    public ChatInfoDto getChatData(
            @AuthenticationPrincipal SeaSearchUser user,
            @PathVariable("chatId") Long chatId
    ) throws SeaSearchClientNotFoundException, InterruptedException {
        return chatService.getChatData(user.getPhoneNumber(), chatId);
    }

    @GetMapping("/chats/ids")
    public Set<Long> getChatData(
            @AuthenticationPrincipal SeaSearchUser user
    ) {
        return userRepository.findById(user.getPhoneNumber()).get().getChatIds();
    }

    @GetMapping("/chats/{chatId}/graph")
    public GraphDto getLineWords(
            @PathVariable("chatId") long chatId,
            @AuthenticationPrincipal SeaSearchUser user
    ) {
        return chatInfoService.getGraph(user.getPhoneNumber(), chatId);
    }


    @GetMapping("/chats/{chatId}/wordcloud")
    public List<DictWordDto> getWordDictionary(
            @PathVariable("chatId") long chatId,
            @AuthenticationPrincipal SeaSearchUser user
    ) {
        return chatInfoService.getDictionaryOfPopularWords(user.getPhoneNumber(), chatId);
    }
}
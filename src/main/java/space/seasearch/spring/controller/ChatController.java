package space.seasearch.spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import space.seasearch.spring.dto.ChatDto;
import space.seasearch.spring.entity.SeaSearchUser;
import space.seasearch.spring.exception.SeaSearchClientNotFoundException;
import space.seasearch.spring.repository.UserRepository;
import space.seasearch.spring.service.TgChatService;
import space.seasearch.telegram.stats.info.InfoStats;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final TgChatService chatService;
    private final UserRepository userRepository;

    @GetMapping("/chats")
    public List<ChatDto> getUserDialogues(
            @AuthenticationPrincipal SeaSearchUser user
    ) throws SeaSearchClientNotFoundException, InterruptedException {
        return chatService.getChats(user.getPhoneNumber());
    }

    @GetMapping("/chats/{chatId}/info")
    public InfoStats getChatData(
            @AuthenticationPrincipal SeaSearchUser user,
            @PathVariable("chatId") Long chatId
    ) throws SeaSearchClientNotFoundException, InterruptedException {
        return chatService.getChatData(user.getPhoneNumber(), chatId);
    }

    @GetMapping("/chats/ids")
    public Set<Long> getChatData(
            @AuthenticationPrincipal SeaSearchUser user
    ) throws SeaSearchClientNotFoundException, InterruptedException {
        return userRepository.findById(user.getPhoneNumber()).get().getChatIds();
    }

//    /**
//     * Фильтрует диалоги по набранной строке пользователем.
//     *
//     * @param search  Поиск пользователя.
//     * @param model   Модель представления, куда необходимо добавлять полученный результат.
//     * @param request Запрос пользователя.
//     * @return Отсортированный список диалогов.
//     */
//    @PostMapping("/dialogs")
//    public String getSortDialogs(@ModelAttribute("search") String search, Model model,
//                                 HttpServletRequest request) {
//        Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);
//
//        if (!tgCacheService.tokenIsPresent(token)) {
//            return "redirect:/login";
//        }
//
//        UserClient userClient = tgCacheService.findUserClientByPhone(token.get());
//        Dialog dialog = userClient.getDialog();
//        dialog.setSearch(search);
//        model.addAttribute("dialog", dialog);
//        model.addAttribute("me", dialog.getUserProfile());
//
//        return "dialog";
//    }
}
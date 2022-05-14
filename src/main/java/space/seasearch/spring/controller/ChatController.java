package space.seasearch.spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import space.seasearch.spring.dto.ChatDto;
import space.seasearch.spring.entity.SeaSearchUser;
import space.seasearch.spring.exception.SeaSearchClientNotFoundException;
import space.seasearch.spring.service.TGCacheService;
import space.seasearch.spring.service.TgChatService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final TgChatService dialoguesService;

    @GetMapping("/chats")
    public List<ChatDto> getUserDialogues(
            @AuthenticationPrincipal SeaSearchUser user
    ) throws SeaSearchClientNotFoundException, InterruptedException {
        return dialoguesService.getDialogues(user.getPhoneNumber());
    }

//    /**
//     * Получает диалоги пользователя и фотки к ними.
//     *
//     * @param model Страница, куда нужно будет занести все необходимое.
//     * @return Страница представления после авторизации.
//     */
//    @GetMapping("/dialogs")
//    public String getDialogs(HttpServletRequest request, Model model) {
//        Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);
//
//        if (!tgCacheService.tokenIsPresent(token)) {
//            return "redirect:/login";
//        }
//
//        UserClient userClient = tgCacheService.findUserClientByPhone(token.get());
//
//        if (userClient.getCurrentStateConstructor() == TdApi.AuthorizationStateReady.CONSTRUCTOR) {
//            Dialog dialog = userClient.getDialog();
//            dialog.setSearch("");
//            dialog.startParse();
//
//            // Заносим все необходимое.
//            model.addAttribute("dialog", dialog);
//            model.addAttribute("me", dialog.getUserProfile());
//            return "dialog";
//        } else {
//            return "redirect:/login";
//        }
//    }

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
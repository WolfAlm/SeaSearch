package space.seasearch.spring.controller;

import it.tdlight.jni.TdApi.AuthorizationStateReady;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import space.seasearch.spring.service.SeaUtils;
import space.seasearch.spring.service.TGCacheService;
import space.seasearch.telegram.communication.chat.Dialog;
import space.seasearch.telegram.user.UserClient;

@Controller
public class DialogController {

  @Value("${TOKEN_NAME}")
  private String cookieTokenKey;
  @Autowired
  private TGCacheService tgCacheService;

  /**
   * Получает диалоги пользователя и фотки к ними.
   *
   * @param model Страница, куда нужно будет занести все необходимое.
   * @return Страница представления после авторизации.
   */
  @GetMapping("/dialogs")
  public String getDialogs(HttpServletRequest request, Model model) {
    Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);

    if (!tgCacheService.tokenIsPresent(token)) {
      return "redirect:/login";
    }

    UserClient userClient = tgCacheService.findUserClientByToken(token.get());

    if (userClient.getCurrentStateConstructor() == AuthorizationStateReady.CONSTRUCTOR) {
      Dialog dialog = userClient.getDialog();
      dialog.setSearch("");
      dialog.startParse();

      // Заносим все необходимое.
      model.addAttribute("dialog", dialog);
      model.addAttribute("me", dialog.getUserProfile());
      return "dialog";
    } else {
      return "redirect:/login";
    }
  }

  /**
   * Фильтрует диалоги по набранной строке пользователем.
   *
   * @param search  Поиск пользователя.
   * @param model   Модель представления, куда необходимо добавлять полученный результат.
   * @param request Запрос пользователя.
   * @return Отсортированный список диалогов.
   */
  @PostMapping("/dialogs")
  public String getSortDialogs(@ModelAttribute("search") String search, Model model,
                               HttpServletRequest request) {
    Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);

    if (!tgCacheService.tokenIsPresent(token)) {
      return "redirect:/login";
    }

    UserClient userClient = tgCacheService.findUserClientByToken(token.get());
    Dialog dialog = userClient.getDialog();
    dialog.setSearch(search);
    model.addAttribute("dialog", dialog);
    model.addAttribute("me", dialog.getUserProfile());

    return "dialog";
  }
}
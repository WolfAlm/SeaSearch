package space.seasearch.spring.controller;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import space.seasearch.spring.service.SeaUtils;
import space.seasearch.spring.service.TGCacheService;
import space.seasearch.spring.service.UserService;
import space.seasearch.telegram.stats.info.InfoStats;
import space.seasearch.telegram.stats.profile.ProfileStats;
import space.seasearch.telegram.user.UserClient;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

  @Value("${TOKEN_NAME}")
  private String cookieTokenKey;
  private TGCacheService tgCacheService;
  private UserService users;

  /**
   * Получает необходимые данные для облака слов.
   *
   * @param id      Идентификатор профиля.
   * @param request Запрос пользователя.
   * @return
   */
  @GetMapping("/{id}/cloudWords")
  public String getCloudWords(@PathVariable("id") long id, HttpServletRequest request) {
    Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);

    if (!tgCacheService.tokenIsPresent(token)) {
      return "redirect:/login";
    }

    UserClient userClient = tgCacheService.findUserClientByPhone(token.get());

    return userClient.getDialog().getProfileStats().get(id).getInfoStats().jsonWords();
  }

  /**
   * Получает необходимые данные для составления графиков по дням.
   *
   * @param id      Идентификатор профиля.
   * @param request Запрос пользователя.
   * @return
   */
  @GetMapping("/{id}/lineWords")
  public String getLineWords(@PathVariable("id") long id, HttpServletRequest request) {
    Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);

    if (!tgCacheService.tokenIsPresent(token)) {
      return "redirect:/login";
    }

    UserClient userClient = tgCacheService.findUserClientByPhone(token.get());
    return userClient.getDialog().getProfileStats().get(id).getInfoStats().jsonMessages();
  }

  @GetMapping("/{id}/restart")
  public String restartProfile(@PathVariable("id") long id, HttpServletRequest request) {
    Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);

    if (!tgCacheService.tokenIsPresent(token)) {
      return "redirect:/login";
    }

    UserClient userClient = tgCacheService.findUserClientByPhone(token.get());

    ProfileStats stats = userClient.getDialog().getProfileStats().get(id);

    stats.setInfoStats(new InfoStats());
    String username = userClient.getDialog().getUserProfile().getUser().username;
    int newestSavedMessageDate = users.updateStats(stats, username, id);
    stats.restartMessage(newestSavedMessageDate);

    return "{}";
  }

  /**
   * Получает необходимого пользователя и запускает процесс получения сообщений.
   *
   * @param id      Идентификатор профиля.
   * @param request Запрос пользователя.
   * @return
   */
  @GetMapping("/{id}")
  public String getProfile(@PathVariable("id") long id, Model model, HttpServletRequest request) {
    Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);

    if (!tgCacheService.tokenIsPresent(token)) {
      return "redirect:/login";
    }

    UserClient userClient = tgCacheService.findUserClientByPhone(token.get());

    ProfileStats stats = userClient.getDialog().getProfileStats().get(id);

    if (stats == null) {
      throw new NotFoundException();
    }

    String username = userClient.getDialog().getUserProfile().getUser().username;
    int newestSavedMessageDate = users.updateStats(stats, username, id);
    stats.parseMessage(newestSavedMessageDate);

    model.addAttribute("profileStats", stats);
    model.addAttribute("me", userClient.getDialog().getUserProfile());

    return "profile";
  }

  /**
   * Обновляет информацию о текущем прогрессе получения сообщений.
   *
   * @param id      Идентификатор профиля.
   * @param request Запрос пользователя.
   * @return Прогресс о результате данных.
   */
  @GetMapping("/{id}/progress")
  public ModelAndView getProgress(@PathVariable("id") long id, HttpServletRequest request) {
    Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);

//    if (!tgCacheService.tokenIsPresent(token)) {
//      return "redirect:/login";
//    }

    UserClient userClient = tgCacheService.findUserClientByPhone(token.get());

    ProfileStats stats = userClient.getDialog().getProfileStats().get(id);
    ModelAndView modelAndView;

    if (stats.isHaveAllMessage()) {
      modelAndView = new ModelAndView("fragments/stats::success-parse");
      stats.updateInfo();
      modelAndView.addObject("profileStats", stats);
      users.saveUser(userClient.getDialog().getUserProfile().getUser().username, id, stats);
    } else {
      modelAndView = new ModelAndView("fragments/stats::proccess-parse");
      modelAndView.addObject("countMessage", stats.getCountAllMessage());
      modelAndView.addObject("date", stats.getDateLastMessage());
    }

    return modelAndView;
  }
}

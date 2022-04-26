package space.seasearch.spring.controller;

import it.tdlight.jni.TdApi.AuthorizationStateReady;
import it.tdlight.jni.TdApi.AuthorizationStateWaitCode;
import it.tdlight.jni.TdApi.AuthorizationStateWaitPassword;
import it.tdlight.jni.TdApi.AuthorizationStateWaitPhoneNumber;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.annotation.RequestScope;
import space.seasearch.spring.entity.InputTelegram;
import space.seasearch.spring.service.SeaUtils;
import space.seasearch.spring.service.TGCacheService;
import space.seasearch.telegram.user.UserClient;

@Controller
@RequestScope
public class AuthenticationController {

  /**
   * На какой срок куки будут актуальными.
   */
  @Value("${COOKIE_MAX_AGE}")
  private int cookieMaxAge;

  @Value("${TOKEN_NAME}")
  private String cookieTokenKey;
  @Autowired
  private TGCacheService tgCacheService;

  /**
   * Производит процесс выхода пользователя из веб-приложения.
   *
   * @param request Запрос, с которого нужно прочитать куки пользователя.
   * @return После выхода пользователя, его перенаправляет на главную страницу.
   */
  @RequestMapping("/exit")
  public String exitProfile(HttpServletRequest request) {
    Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);
    UserClient userClient = tgCacheService.findUserClientByToken(token.get());
    tgCacheService.deleteUserByToken(token.get());
    userClient.exitUser();

    return "redirect:/";
  }

  /**
   * Отображение информации о веб-приложении.
   *
   * @return Информационная страница.
   */
  @GetMapping("/info")
  public String viewInfoPage() {
    return "info";
  }

  /**
   * Проверяет, была ли ранее у пользователя авторизация, и если нет, то оставляет пользователя на
   * форме авторизации, иначе – перенаправляет к списку диалогов.
   *
   * @param request Запрос, с которого нужно прочитать куки пользователя.
   * @param model   Модель представления страницы, куда нужно занести форму информации.
   * @return
   */
  @GetMapping("/login")
  public String registration(HttpServletRequest request, Model model) {
    Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);
    InputTelegram inputTelegram = new InputTelegram();

    if (tgCacheService.tokenIsPresent(token)) {
      UserClient userClient = tgCacheService.findUserClientByToken(token.get());

      switch (userClient.getCurrentStateConstructor()) {
        case AuthorizationStateReady.CONSTRUCTOR:
          return "redirect:/dialogs";
        case AuthorizationStateWaitCode.CONSTRUCTOR:
          inputTelegram.setSteps(new Boolean[]{false, true, false});
          break;
        case AuthorizationStateWaitPassword.CONSTRUCTOR:
          inputTelegram.setSteps(new Boolean[]{false, false, true});
          break;
      }
    }

    model.addAttribute("inputTelegram", inputTelegram);
    return "login";
  }

  /**
   * @param inputTelegram Модель, где можно узнать ввод пользователя.
   * @param errorsBinding Модель, содержащие ошибки.
   * @param request       Запрос пользователя.
   * @param response      Ответ пользователю.
   * @return Результаты процесса авторизации.
   * @throws InterruptedException
   */
  @PostMapping("/login")
  public String processAuth(@ModelAttribute("inputTelegram") @Valid InputTelegram inputTelegram,
      BindingResult errorsBinding, HttpServletRequest request, HttpServletResponse response)
      throws InterruptedException {
    Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);
    UserClient userClient;
    CountDownLatch countDownLatch = new CountDownLatch(1);

    if (!tgCacheService.tokenIsPresent(token)) {
      String secretToken = tgCacheService.generateToken();
      Cookie cookie = new Cookie(cookieTokenKey, secretToken);
      cookie.setMaxAge(cookieMaxAge);
      cookie.setHttpOnly(true);
      response.addCookie(cookie);
      // Check again if user logged in on another device
      if (!tgCacheService.tokenExist(secretToken)) {
        userClient = tgCacheService.registerToken(secretToken, countDownLatch);
        countDownLatch.await();
        countDownLatch = new CountDownLatch(1);
        userClient.setCountDownLatch(countDownLatch);
      } else {
        userClient = tgCacheService.findUserClientByToken(secretToken);
        userClient.setCountDownLatch(countDownLatch);
      }
    } else {
      userClient = tgCacheService.findUserClientByToken(token.get());
      userClient.setCountDownLatch(countDownLatch);
    }

    // Проверяем состояние пользователя.
    switch (userClient.getCurrentStateConstructor()) {
      case AuthorizationStateReady.CONSTRUCTOR:
        return "redirect:/dialogs";
      case AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
        if (errorsBinding.hasFieldErrors("phoneNumber")) {
          return "login";
        } else {
          userClient.authPhoneNumber(inputTelegram.getPhoneNumber());

          countDownLatch.await();

          switch (userClient.getCurrentError()) {
            case "PHONE_NUMBER_INVALID":
              errorsBinding.rejectValue("phoneNumber", "error.userTelegram",
                  "Такой номер неподходящего формата!");
              return "login";
            case "":
              inputTelegram.setSteps(new Boolean[]{false, true, false});
              return "login";
            default:
              errorsBinding
                  .rejectValue("phoneNumber", "error.userTelegram", userClient.getCurrentError());
              return "login";
          }
        }
      case AuthorizationStateWaitCode.CONSTRUCTOR:
        userClient.authCode(inputTelegram.getCode());

        countDownLatch.await();
        inputTelegram.setSteps(new Boolean[]{false, true, false});
        System.out.println(userClient.getCurrentError());
        switch (userClient.getCurrentError()) {
          case "PHONE_CODE_INVALID":
            errorsBinding.rejectValue("code", "error.userTelegram",
                "Неправильно введен код!");
            return "login";
          case "":
            if (userClient.getCurrentStateConstructor()
                == AuthorizationStateWaitPassword.CONSTRUCTOR) {
              inputTelegram.setSteps(new Boolean[]{false, false, true});
              return "login";
            } else {
              return "redirect:/dialogs";
            }
          default:
            errorsBinding.rejectValue("code", "error.userTelegram", userClient.getCurrentError());
            return "login";
        }
      case AuthorizationStateWaitPassword.CONSTRUCTOR:
        userClient.authPassword(inputTelegram.getPassword());
        inputTelegram.setPassword("");

        countDownLatch.await();

        inputTelegram.setSteps(new Boolean[]{false, false, true});
        switch (userClient.getCurrentError()) {
          case "PASSWORD_HASH_INVALID":
            errorsBinding.rejectValue("password", "error.userTelegram",
                "Неправильно введен пароль!");
            return "login";
          case "":
            if (userClient.getCurrentStateConstructor()
                == AuthorizationStateWaitPassword.CONSTRUCTOR) {
              return "login";
            } else {
              return "redirect:/dialogs";
            }
          default:
            errorsBinding
                .rejectValue("password", "error.userTelegram", userClient.getCurrentError());
            return "login";
        }
    }

    return "login";
  }
}
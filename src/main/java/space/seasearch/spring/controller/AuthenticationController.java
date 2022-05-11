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

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import space.seasearch.spring.entity.TelegramInputDto;
import space.seasearch.spring.jwt.JwtService;
import space.seasearch.spring.service.SeaUtils;
import space.seasearch.spring.service.TGCacheService;
import space.seasearch.spring.service.TelegramAuthService;
import space.seasearch.telegram.user.UserClient;

@RestController
@RequestScope
@RequiredArgsConstructor
public class AuthenticationController {

    /**
     * На какой срок куки будут актуальными.
     */
    @Value("${COOKIE_MAX_AGE}")
    private int cookieMaxAge;

    @Value("${TOKEN_NAME}")
    private String cookieTokenKey;
    private TGCacheService tgCacheService;
    private final TelegramAuthService telegramAuthService;
    private final JwtService jwtService;

    /**
     * Производит процесс выхода пользователя из веб-приложения.
     *
     * @param request Запрос, с которого нужно прочитать куки пользователя.
     * @return После выхода пользователя, его перенаправляет на главную страницу.
     */
    @RequestMapping("/exit")
    public String exitProfile(HttpServletRequest request) {
        Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);
        UserClient userClient = tgCacheService.findUserClientByPhone(token.get());
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
        TelegramInputDto inputTelegram = new TelegramInputDto();

        if (tgCacheService.tokenIsPresent(token)) {
            UserClient userClient = tgCacheService.findUserClientByPhone(token.get());

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

    @PostMapping("/login/phone")
    public void loginWithPhoneNumber(
            @RequestBody @Valid TelegramInputDto telegramInputDto,
            HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        var user = telegramAuthService.processNewUser(telegramInputDto.getPhoneNumber());
        jwtService.insertJwtTokens(user.getUsername(), response, request);
    }

//    /**
//     * @param inputTelegram Модель, где можно узнать ввод пользователя.
//     * @param errorsBinding Модель, содержащие ошибки.
//     * @param request       Запрос пользователя.
//     * @param response      Ответ пользователю.
//     * @return Результаты процесса авторизации.
//     * @throws InterruptedException
//     */
//    @PostMapping("/login")
//    public String processAuth(@ModelAttribute("inputTelegram") @Valid TelegramInputDto inputTelegram,
//                              BindingResult errorsBinding, HttpServletRequest request, HttpServletResponse response)
//            throws InterruptedException {
//        Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);
//        UserClient userClient;
//        CountDownLatch countDownLatch = new CountDownLatch(1);

//
//        // Проверяем состояние пользователя.
//        switch (userClient.getCurrentStateConstructor()) {
//            case AuthorizationStateReady.CONSTRUCTOR:
//                return "redirect:/dialogs";
//            case AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
//                if (errorsBinding.hasFieldErrors("phoneNumber")) {
//                    return "login";
//                } else {
//                    userClient.authPhoneNumber(inputTelegram.getPhoneNumber());
//
//                    countDownLatch.await();
//
//                    switch (userClient.getCurrentError()) {
//                        case "PHONE_NUMBER_INVALID":
//                            errorsBinding.rejectValue("phoneNumber", "error.userTelegram",
//                                    "Такой номер неподходящего формата!");
//                            return "login";
//                        case "":
//                            inputTelegram.setSteps(new Boolean[]{false, true, false});
//                            return "login";
//                        default:
//                            errorsBinding
//                                    .rejectValue("phoneNumber", "error.userTelegram", userClient.getCurrentError());
//                            return "login";
//                    }
//                }
//            case AuthorizationStateWaitCode.CONSTRUCTOR:
//                userClient.authCode(inputTelegram.getCode());
//
//                countDownLatch.await();
//                inputTelegram.setSteps(new Boolean[]{false, true, false});
//                System.out.println(userClient.getCurrentError());
//                switch (userClient.getCurrentError()) {
//                    case "PHONE_CODE_INVALID":
//                        errorsBinding.rejectValue("code", "error.userTelegram",
//                                "Неправильно введен код!");
//                        return "login";
//                    case "":
//                        if (userClient.getCurrentStateConstructor()
//                                == AuthorizationStateWaitPassword.CONSTRUCTOR) {
//                            inputTelegram.setSteps(new Boolean[]{false, false, true});
//                            return "login";
//                        } else {
//                            return "redirect:/dialogs";
//                        }
//                    default:
//                        errorsBinding.rejectValue("code", "error.userTelegram", userClient.getCurrentError());
//                        return "login";
//                }
//            case AuthorizationStateWaitPassword.CONSTRUCTOR:
//                userClient.authPassword(inputTelegram.getPassword());
//                inputTelegram.setPassword("");
//
//                countDownLatch.await();
//
//                inputTelegram.setSteps(new Boolean[]{false, false, true});
//                switch (userClient.getCurrentError()) {
//                    case "PASSWORD_HASH_INVALID":
//                        errorsBinding.rejectValue("password", "error.userTelegram",
//                                "Неправильно введен пароль!");
//                        return "login";
//                    case "":
//                        if (userClient.getCurrentStateConstructor()
//                                == AuthorizationStateWaitPassword.CONSTRUCTOR) {
//                            return "login";
//                        } else {
//                            return "redirect:/dialogs";
//                        }
//                    default:
//                        errorsBinding
//                                .rejectValue("password", "error.userTelegram", userClient.getCurrentError());
//                        return "login";
//                }
//        }
//
//        return "login";
//    }
    }
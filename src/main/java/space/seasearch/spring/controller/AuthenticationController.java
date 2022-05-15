package space.seasearch.spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import space.seasearch.spring.dto.AuthenticationDto;
import space.seasearch.spring.entity.SeaSearchUser;
import space.seasearch.spring.entity.TelegramInputDto;
import space.seasearch.spring.jwt.JwtService;
import space.seasearch.spring.service.TelegramAuthDtoValidator;
import space.seasearch.spring.service.TelegramAuthService;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
@RequestScope
@RequiredArgsConstructor
public class AuthenticationController {

    private final TelegramAuthService telegramAuthService;
    private final JwtService jwtService;
    private final TelegramAuthDtoValidator validator;

    //FIXME: remove when releasing
    @GetMapping("/token/{phoneNum}")
    public AuthenticationDto getTokens(
            @PathVariable("phoneNum") String phoneNum,
            HttpServletRequest request
    ) throws Exception {
        return jwtService.getTokens(phoneNum, request);
    }

    @RequestMapping("/logout")
    public void exitProfile(
            @AuthenticationPrincipal SeaSearchUser user
    ) throws Exception {
        telegramAuthService.logoutUser(user.getPhoneNumber());
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


    @PostMapping("/login/phone")
    public ResponseEntity<AuthenticationDto> loginWithPhoneNumber(
            @RequestBody TelegramInputDto telegramInputDto,
            HttpServletRequest request
    ) throws Exception {
        validator.validatePhoneNumber(telegramInputDto.getPhoneNumber());
        var user = telegramAuthService.processNewUser(telegramInputDto.getPhoneNumber());
        var authDto = jwtService.getTokens(user.getPhoneNumber(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authDto);
    }

    @PostMapping("/login/code")
    public void verifyLoginWithCode(
            @RequestBody TelegramInputDto telegramInputDto,
            @AuthenticationPrincipal SeaSearchUser user
    ) throws Exception {
        validator.validateCode(telegramInputDto.getCode());
        telegramAuthService.authenticateUserWithCode(user.getPhoneNumber(), telegramInputDto.getCode());
    }

    @PostMapping("/login/password")
    public void verifyLoginWithPassword(
            @RequestBody TelegramInputDto telegramInputDto,
            @AuthenticationPrincipal SeaSearchUser user
    ) throws Exception {
        validator.validatePassword(telegramInputDto.getPassword());
        telegramAuthService.authenticateWithPassword(user.getPhoneNumber(), telegramInputDto.getPassword());
    }
}
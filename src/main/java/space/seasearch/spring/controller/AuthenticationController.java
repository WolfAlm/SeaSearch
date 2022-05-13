package space.seasearch.spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import space.seasearch.spring.dto.AuthenticationDto;
import space.seasearch.spring.entity.SeaSearchUser;
import space.seasearch.spring.entity.TelegramInputDto;
import space.seasearch.spring.jwt.JwtService;
import space.seasearch.spring.service.TelegramAuthDtoValidator;
import space.seasearch.spring.service.TelegramAuthService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestScope
@RequiredArgsConstructor
public class AuthenticationController {

    private final TelegramAuthService telegramAuthService;
    private final JwtService jwtService;
    private final TelegramAuthDtoValidator validator;


    @RequestMapping("/logout")
    public void exitProfile() throws Exception {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        SeaSearchUser user = (SeaSearchUser) auth.getPrincipal();
        telegramAuthService.logoutUser(user.getUsername());
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
            @RequestBody @Valid TelegramInputDto telegramInputDto,
            HttpServletRequest request
    ) throws Exception {
        validator.validatePhoneNumber(telegramInputDto.getPhoneNumber());
        var user = telegramAuthService.processNewUser(telegramInputDto.getPhoneNumber());
        var authDto = jwtService.getTokens(user.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authDto);
    }

    @PostMapping("/login/code")
    public ResponseEntity verifyLoginWithCode(
            @RequestBody @Valid TelegramInputDto telegramInputDto
    ) throws Exception {
        validator.validateCode(telegramInputDto.getCode());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        SeaSearchUser user = (SeaSearchUser) auth.getPrincipal();
        return telegramAuthService.authenticateUserWithCode(user.getUsername(), telegramInputDto.getCode());
    }

    @PostMapping("/login/password")
    public void verifyLoginWithPassword(
            @RequestBody @Valid TelegramInputDto telegramInputDto
    ) throws Exception {
        validator.validatePassword(telegramInputDto.getPassword());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        SeaSearchUser user = (SeaSearchUser) auth.getPrincipal();
        telegramAuthService.authenticateWithPassword(user.getUsername(), telegramInputDto.getPassword());
    }
}
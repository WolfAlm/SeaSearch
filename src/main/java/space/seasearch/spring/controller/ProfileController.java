package space.seasearch.spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import space.seasearch.spring.dto.GraphDto;
import space.seasearch.spring.entity.SeaSearchUser;
import space.seasearch.spring.service.ChatInfoService;
import space.seasearch.spring.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private UserService users;




}

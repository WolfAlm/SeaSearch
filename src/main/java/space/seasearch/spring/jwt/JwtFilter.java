package space.seasearch.spring.jwt;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import space.seasearch.spring.repository.UserRepository;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final List<String> jwtWhitelist = List.of("/login/phone", "/");
    private final List<String> resources = List.of("/static", "/templates", "/img", "/js", "/style", "/fragments", "/token");
    private final JwtService jwtService;
    private final UserRepository userRepository;

    private final static String BEARER = "Bearer ";


    @SneakyThrows({TokenException.class, JwtException.class})
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        var subject = verifyTokenValidity(request);
        var user = userRepository.findById(subject).orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.createAuthorityList("ROLE_USER")));

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return jwtWhitelist.contains(request.getServletPath()) || resources.stream().anyMatch(it -> request.getServletPath().startsWith(it));
    }

    private String verifyTokenValidity(HttpServletRequest request) throws TokenException, JwtException {
        if (request.getHeader(HttpHeaders.AUTHORIZATION) == null || request.getHeader(HttpHeaders.AUTHORIZATION).isEmpty()) {
            throw new TokenException("Header does not contain a token");
        }

        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!authHeader.startsWith(BEARER)) {
            throw new TokenException("Token is not a bearer type token");
        }

        var token = authHeader.substring(BEARER.length());
        var decodedToken = jwtService.decodeJwt(token);
        return decodedToken.getSubject();
    }
}
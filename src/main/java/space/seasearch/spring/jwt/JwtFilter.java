package space.seasearch.spring.jwt;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final List<String> jwtWhitelist;
    private final JwtService jwtService;

    private final static String BEARER = "Bearer ";

    private final AuthenticationFailureHandler failureHandler =
            new AuthenticationEntryPointFailureHandler(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));


    @SneakyThrows({TokenException.class, JwtException.class})
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        verifyToken(request);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return jwtWhitelist.contains(request.getServletPath());
    }

    private void verifyToken(HttpServletRequest request) throws TokenException, JwtException {
        var token = verifyTokenValidity(request);
        verifyResourceAccess(token, request);
    }

    private void verifyResourceAccess(String token, HttpServletRequest request) throws TokenException {
        String path = request.getServletPath();
        String[] pathParts = path.split("/");

        if (pathParts.length <= 2 || !pathParts[2].equals(token)) {
            throw new TokenException("Path within the token does not correspond to a resource path");
        }
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
        return decodedToken.getToken();
    }


    private void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            AuthenticationException failed) throws IOException, ServletException {
        this.failureHandler.onAuthenticationFailure(request, response, failed);
    }
}
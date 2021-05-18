package space.seasearch.spring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import space.seasearch.spring.service.SeaUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import space.seasearch.telegram.photo.PhotoPath;

@Component
public class DatabaseUserAccessFilter extends OncePerRequestFilter {

  @Value("${TOKEN_NAME}")
  private String cookieTokenKey;

  private final AuthenticationFailureHandler failureHandler =
      new AuthenticationEntryPointFailureHandler(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    Optional<String> token = SeaUtils.readServletCookie(request, cookieTokenKey);

    if (token.isEmpty() || token.get().isEmpty()) {
      unsuccessfulAuthentication(request, response,
          new AuthenticationException("Invalid access") {
            @Override
            public String getMessage() {
              return super.getMessage();
            }
          });
      return;
    }

    String path = request.getServletPath();
    String[] pathParts = path.split("/");

    if (pathParts.length <= 2 || !pathParts[2].equals(token.get())) {
      unsuccessfulAuthentication(
          request,
          response,
          new AuthenticationException("Invalid access") {
            @Override
            public String getMessage() {
              return super.getMessage();
            }
          });
      return;
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();

    return !path.startsWith(PhotoPath.PATH_TO_DATABASE);
  }

  private void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException failed) throws IOException, ServletException {
    this.failureHandler.onAuthenticationFailure(request, response, failed);
  }
}
package space.seasearch.spring.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class FilterExceptionHandler extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (Exception exception) {
            httpServletResponse.setContentType("application/json");
            if (exception.getClass().isAssignableFrom(TokenException.class)) {
                httpServletResponse.sendError(401, exception.getMessage());
            } else {
                httpServletResponse.sendError(501, exception.getMessage());
            }
        }
    }
}

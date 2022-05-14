package space.seasearch.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import space.seasearch.spring.entity.SeaSearchUser;

import java.util.List;

@Configuration
public class SeaSearchWebMvcConfiguration extends DelegatingWebMvcConfiguration {

    @Bean
    public UserArgumentResolver<SeaSearchUser> userArgumentResolver() {
        return new UserArgumentResolver<>(SeaSearchUser.class);
    }

    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userArgumentResolver());
    }
}

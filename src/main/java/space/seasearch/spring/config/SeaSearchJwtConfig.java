package space.seasearch.spring.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeaSearchJwtConfig {

    @Value("${app.jwt.secret}")
    private String secret;

    @Bean
    public Algorithm jwtEncoder() {
        return Algorithm.HMAC256(secret);
    }

    @Bean
    public JWTVerifier jwtVerifier() {
        return JWT.require(jwtEncoder()).build();
    }

}

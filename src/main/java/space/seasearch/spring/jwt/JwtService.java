package space.seasearch.spring.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import space.seasearch.spring.dto.AuthenticationDto;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final Algorithm jwtEncoder;
    private final JWTVerifier jwtVerifier;
    @Value("${app.access.expiration}")
    private long accessTokenExpiration;
    @Value("${app.refresh.expiration}")
    private long refreshTokenExpiration;

    public String createAccessToken(String phoneNumber, HttpServletRequest request) {
        return createToken(phoneNumber, expireIn(accessTokenExpiration), request.getRequestURL().toString());
    }

    public String createRefreshToken(String phoneNumber, HttpServletRequest request) {
        return createToken(phoneNumber, expireIn(refreshTokenExpiration), request.getRequestURL().toString());
    }

    public AuthenticationDto getTokens(String phoneNumber, HttpServletRequest request) {
        return new AuthenticationDto(createAccessToken(phoneNumber, request), createRefreshToken(phoneNumber, request));
    }

    public AuthenticationDto updateTokens(String token) {
        return new AuthenticationDto(updateAccessToken(token), updateRefreshToken(token));
    }


    public String updateAccessToken(String token) throws JwtException {
        var decodedToken = decodeJwt(token);
        return createToken(decodedToken.getSubject(), expireIn(accessTokenExpiration), decodedToken.getIssuer());
    }

    public String updateRefreshToken(String token) throws JwtException {
        var decodedToken = decodeJwt(token);
        return createToken(decodedToken.getSubject(), expireIn(refreshTokenExpiration), decodedToken.getIssuer());
    }

    public DecodedJWT decodeJwt(String token) throws JwtException {
        try {
            return jwtVerifier.verify(token);
        } catch (JWTVerificationException exception) {
            throw new JwtException(exception.getMessage());
        }
    }


    private Date expireIn(long expiresIn) {
        return new Date(System.currentTimeMillis() + expiresIn * 1000 * 60);
    }

    private String createToken(String phoneNumber, Date expirationDate, String issuer) {
        return JWT.create()
                .withSubject(phoneNumber)
                .withExpiresAt(expirationDate)
                .withIssuer(issuer)
                .sign(jwtEncoder);
    }

}

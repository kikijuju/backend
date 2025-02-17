package com.hanbat.tcar.common;

import com.hanbat.tcar.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;


@Component
public class JwtGenerator {
    private final Key SECRET_KEY;
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15; // 15분
    private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7일

    public JwtGenerator(@Value("${jwt.secret.key}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
    }

    public JWToken generateToken(User user){
        Date now = new Date();
        Date accessTokenExpiry = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);
        Date refreshTokenExpiry = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);

        // 액세스 토큰 생성
        //여기 최신 정보로 봐야함. 블로그대로 하면 옛날 거임.
        //https://github.com/jwtk/jjwt#quickstart
        String accessToken = Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("userName", user.getUsername())
                .issuedAt(now)
                .expiration(accessTokenExpiry)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("userName", user.getUsername())
                .issuedAt(now)
                .expiration(refreshTokenExpiry)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();

        return JWToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }
}

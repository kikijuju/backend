package com.hanbat.tcar.security.jwt;

import com.hanbat.tcar.user.entity.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;


@Component
public class JwtGenerator {
    // 로그인/인가용 키
    private final Key SECRET_KEY;
    // 컨테이너 전용(pre-signed URL) 키
    private final Key CONTAINER_SECRET_KEY;
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15; // 15분
    private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7;// 7일
    private final long CONTAINER_TOKEN_EXPIRATION = 1000 * 60 * 5; // 5분

    // 생성자에서 두 가지 키를 모두 주입받음
    public JwtGenerator(
            @Value("${jwt.secret.key}") String secretKey,
            @Value("${pre-signed.url}") String containerKey
    ) {
        // 로그인/인가용 키
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);

        // 컨테이너 전용 키
        byte[] containerKeyBytes = Decoders.BASE64.decode(containerKey);
        this.CONTAINER_SECRET_KEY = Keys.hmacShaKeyFor(containerKeyBytes);
    }

    /**
     * 일반 Access/Refresh Token 생성
     */
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
                .signWith(SECRET_KEY)
                .compact();

        io.jsonwebtoken.JwtBuilder builder = Jwts.builder();
        builder.subject(user.getEmail());
        builder.claim("userId", user.getId());
        builder.claim("userName", user.getUsername());
        builder.issuedAt(now);
        builder.expiration(refreshTokenExpiry);
        builder.signWith(SECRET_KEY);
        String refreshToken = builder
                .compact();

        return JWToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    /**
     * 컨테이너 전용(pre-signed URL) 토큰 생성
     */
    public String generateTokenWithContainerInfo(User user, String containerId, String port) {
        Date now = new Date();
        Date containerTokenExpiry = new Date(now.getTime() + CONTAINER_TOKEN_EXPIRATION);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("containerId", containerId)
                .claim("port", port)
                .issuedAt(now)
                .expiration(containerTokenExpiry)
                // 컨테이너 전용 토큰은 CONTAINER_SECRET_KEY 사용
                .signWith(CONTAINER_SECRET_KEY)
                .compact();
    }
}
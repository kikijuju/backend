package com.hanbat.tcar.auth.jwt;

import com.hanbat.tcar.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtGenerator {

    /* ───── 키 & 만료시간 ───── */
    private final Key SECRET_KEY;                    // 로그인/인가용 키
    private final Key CONTAINER_SECRET_KEY;          // 컨테이너 전용 키

    // 예시: Access 30분, Refresh 7일 (값은 원하는 대로 조정)
    private final long ACCESS_TOKEN_EXPIRATION_MS  = 1000L * 60 * 30;         // 30분
    private final long REFRESH_TOKEN_EXPIRATION_MS = 1000L * 60 * 60 * 24 * 7; // 7일
    private final long CONTAINER_TOKEN_EXPIRATION  = 1000L * 60 * 5;           // 5분

    public JwtGenerator(
            @Value("${jwt.secret.key}") String secretKey,
            @Value("${pre-signed.url}") String containerKey) {

        this.SECRET_KEY           = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.CONTAINER_SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(containerKey));
    }

    /* ───────────────────────── Access + Refresh 생성 ───────────────────────── */
    public JWToken generateToken(User user) {

        Date now = new Date();

        // Access
        Date accessExp = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_MS);
        String accessToken = Jwts.builder()
                .subject(user.getEmail())
                .claim("userId",   user.getId())
                .claim("userName", user.getUsername())
                .issuedAt(now)
                .expiration(accessExp)
                .signWith(SECRET_KEY)
                .compact();

        // Refresh (payload는 최소화)
        Date refreshExp = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_MS);
        String refreshToken = Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(now)
                .expiration(refreshExp)
                .signWith(SECRET_KEY)
                .compact();

        return JWToken.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn(ACCESS_TOKEN_EXPIRATION_MS / 1000) // 초 단위
                .refreshToken(refreshToken)
                .build();
    }

    /* ─────────────────────── 컨테이너 전용 토큰 ─────────────────────── */
    public String generateTokenWithContainerInfo(
            User user, String podName, String podNamespace, String ingress) {

        Date now = new Date();
        Date exp = new Date(now.getTime() + CONTAINER_TOKEN_EXPIRATION);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("podName",      podName)
                .claim("podNamespace", podNamespace)
                .claim("ingress",      ingress)
                .issuedAt(now)
                .expiration(exp)
                .signWith(CONTAINER_SECRET_KEY)
                .compact();
    }
}

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

    private final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7일
    // private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // ❌ 사용 안 함
    private final long CONTAINER_TOKEN_EXPIRATION = 1000 * 60 * 5;          // 5분

    public JwtGenerator(
            @Value("${jwt.secret.key}") String secretKey,
            @Value("${pre-signed.url}") String containerKey) {

        this.SECRET_KEY          = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.CONTAINER_SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(containerKey));
    }

    /* ───────────────────────── Access Token만 생성 ───────────────────────── */
    public JWToken generateToken(User user) {

        Date now   = new Date();
        Date exp   = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

        String accessToken = Jwts.builder()
                .subject(user.getEmail())
                .claim("userId",   user.getId())
                .claim("userName", user.getUsername())
                .issuedAt(now)
                .expiration(exp)
                .signWith(SECRET_KEY)
                .compact();

        /* refreshToken 로직 제거/주석 처리
        Date refreshExp = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);
        String refreshToken = Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(now)
                .expiration(refreshExp)
                .signWith(SECRET_KEY)
                .compact();
        */

        return JWToken.builder()
                .accessToken(accessToken)
                // .refreshToken(refreshToken)   // ❌ 더 이상 사용하지 않음
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
package com.hanbat.tcar.common;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtProvider {
    private final SecretKey SECRET_KEY;

    public JwtProvider(@Value("${jwt.secret.key}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes); // SecretKey로 저장
    }

    // JWT 검증 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);

            return true;  // 유효한 토큰
        } catch (ExpiredJwtException e) {
            System.out.println("JWT 만료됨");
        } catch (MalformedJwtException e) {
            System.out.println("JWT 형식 오류");
        } catch (Exception e) {
            System.out.println("JWT 검증 실패: " + e.getMessage());
        }
        return false;
    }


    // 토큰에서 이메일(사용자 정보) 추출
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)  // `setSigningKey()` 대신 사용
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}

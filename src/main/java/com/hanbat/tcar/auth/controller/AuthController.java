package com.hanbat.tcar.auth.controller;

import com.hanbat.tcar.auth.jwt.JWToken;
import com.hanbat.tcar.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Refresh Token을 이용한 Access Token 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<JWToken> refresh(
            @RequestHeader("Refresh-Token") String refreshToken) {

        JWToken newTokens = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(newTokens);           // JSON → 프론트 저장
    }


    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(Authentication authentication) {
        if (authentication == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않은 토큰");
        return ResponseEntity.ok("인증된 사용자: " + authentication.getName());
    }
}
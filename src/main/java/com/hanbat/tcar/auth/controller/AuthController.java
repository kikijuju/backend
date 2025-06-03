package com.hanbat.tcar.auth.controller;

import com.hanbat.tcar.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<String> refreshAccessToken(HttpServletRequest request,
                                                     HttpServletResponse response) {
        String message = authService.refreshToken(request, response);
        return ResponseEntity.ok(message);
    }

    /**
     * 현재 인증된 사용자 확인 (Access Token 유효성 체크)
     */
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("인증 실패: 토큰이 없거나 유효하지 않습니다.");
        }

        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok("유효한 토큰. 이메일: " + email);
    }
}
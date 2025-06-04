package com.hanbat.tcar.auth.service;

import com.hanbat.tcar.auth.jwt.JWToken;
import com.hanbat.tcar.auth.jwt.JwtGenerator;
import com.hanbat.tcar.user.UserRepository;
import com.hanbat.tcar.user.dto.UserLoginRequestDto;
import com.hanbat.tcar.user.entity.User;
import com.hanbat.tcar.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Variable 방식:
 *   • AccessToken / RefreshToken  → JSON 페이로드
 *   • RefreshToken               → Redis 로 관리
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtGenerator jwtGenerator;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    /* 1) 로그인 ─────────────────────────────────────────────── */
    public JWToken login(UserLoginRequestDto dto) {

        User user = userService.userFind(dto);              // 이메일·비번 검증
        JWToken token = jwtGenerator.generateToken(user);   // Access / Refresh 생성

        // RefreshToken → Redis (7일)
        refreshTokenService.storeRefreshToken(
                user.getId(),
                token.getRefreshToken(),
                7L);

        return token;                                       // ★ JSON 그대로 반환
    }

    /* 2) AccessToken 재발급 ─────────────────────────────────── */
    public JWToken refreshToken(String refreshToken) {

        Long uid = refreshTokenService.getUserIdByRefreshToken(refreshToken);
        if (uid == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid refresh");

        User user = userRepository.findById(uid).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found"));

        JWToken newToken = jwtGenerator.generateToken(user);

        // (선택) RefreshToken도 갱신·연장
        refreshTokenService.storeRefreshToken(
                uid,
                newToken.getRefreshToken(),
                7L);

        return newToken;                                    // ★ JSON
    }

    /* 3) 로그아웃 ───────────────────────────────────────────── */
    public void logout(String refreshToken) {
        if (refreshToken != null)
            refreshTokenService.deleteRefreshToken(refreshToken); // Redis 삭제
    }
}
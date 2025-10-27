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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtGenerator jwtGenerator;
    private final UserService userService;

    // ★ Redis 등을 사용하는 실제 구현체를 빈으로 주입하세요.
    private final RedisRefreshTokenService refreshTokenService;

    /**
     * 로그인: 유저 검증 → Access/Refresh 발급 → Refresh 저장(7일) → 결과 리턴
     * 컨트롤러에서 Access는 JSON, Refresh는 HttpOnly 쿠키로 내려줌
     */
    public LoginResult loginAndIssue(UserLoginRequestDto dto) {
        User user = userService.userFind(dto);            // 이메일/비번 검증 (예외 던짐)
        JWToken token = jwtGenerator.generateToken(user); // Access + Refresh 생성

        // RefreshToken → Redis 저장 (TTL: 7일)
        refreshTokenService.storeRefreshToken(user.getId(), token.getRefreshToken(), 7L);

        return new LoginResult(
                token.getAccessToken(),
                token.getAccessTokenExpiresIn(),
                token.getRefreshToken(),
                refreshTokenService.getTtlSeconds(token.getRefreshToken()),
                user
        );
    }

    /**
     * Access 재발급: 쿠키에서 받은 refreshToken 검증 → Access 새로 생성
     * (권장) Refresh 로테이션: 구 토큰 삭제 후 신 토큰 저장
     */
    public AccessIssue reissueAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh missing");
        }

        Long uid = refreshTokenService.getUserIdByRefreshToken(refreshToken);
        if (uid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid refresh");
        }

        User user = userRepository.findById(uid).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found")
        );

        JWToken newToken = jwtGenerator.generateToken(user);

        // Refresh 로테이션 (보안 권장)
        refreshTokenService.deleteRefreshToken(refreshToken);
        refreshTokenService.storeRefreshToken(uid, newToken.getRefreshToken(), 7L);

        return new AccessIssue(
                newToken.getAccessToken(),
                newToken.getAccessTokenExpiresIn(),
                newToken.getRefreshToken(),
                refreshTokenService.getTtlSeconds(newToken.getRefreshToken())
        );
    }

    /**
     * 로그아웃: refreshToken 제거
     */
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.deleteRefreshToken(refreshToken);
        }
    }

    // --- 서비스 내부 응답 DTO (record) ---
    public record LoginResult(String accessToken, long accessTokenExpiresIn,
                              String refreshToken, long refreshTtlSeconds, User user) {}

    public record AccessIssue(String accessToken, long accessTokenExpiresIn,
                              String rotatedRefreshToken, long refreshTtlSeconds) {}
}

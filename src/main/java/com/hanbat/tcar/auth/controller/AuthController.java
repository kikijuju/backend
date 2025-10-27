package com.hanbat.tcar.auth.controller;

import com.hanbat.tcar.auth.service.AuthService;
import com.hanbat.tcar.user.dto.UserLoginRequestDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private static final String COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/api"; // 프록시 기준

    /* 로그인: Access(JSON) + Refresh(HttpOnly 쿠키[Lax, Secure=false]) */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserLoginRequestDto dto,
                                               HttpServletResponse res) {
        var result = authService.loginAndIssue(dto);

        ResponseCookie refresh = ResponseCookie.from(COOKIE_NAME, result.refreshToken())
                .httpOnly(true)
                .secure(false)      // HTTP 환경(옵션 A)
                .sameSite("Lax")    // 같은 사이트로 보이므로 OK
                .path(COOKIE_PATH)  // /api 하위에서만 전송
                .maxAge(result.refreshTtlSeconds())
                .build();
        res.addHeader("Set-Cookie", refresh.toString());

        return ResponseEntity.ok(new LoginResponse(
                result.accessToken(),
                result.accessTokenExpiresIn(),
                new LoginUser(result.user().getId(), result.user().getEmail(), result.user().getUsername())
        ));
    }

    /* 재발급: 쿠키에서 refreshToken 읽어 Access 재발급(+로테이션) */
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(
            @CookieValue(name = COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse res) {

        var re = authService.reissueAccessToken(refreshToken);

        if (re.rotatedRefreshToken() != null) {
            ResponseCookie rotated = ResponseCookie.from(COOKIE_NAME, re.rotatedRefreshToken())
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path(COOKIE_PATH)
                    .maxAge(re.refreshTtlSeconds())
                    .build();
            res.addHeader("Set-Cookie", rotated.toString());
        }

        return ResponseEntity.ok(new AccessTokenResponse(
                re.accessToken(), re.accessTokenExpiresIn()
        ));
    }

    /* 로그아웃: 쿠키 제거 + Redis 매핑 삭제 */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse res) {

        authService.logout(refreshToken);

        ResponseCookie clear = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(0)
                .build();
        res.addHeader("Set-Cookie", clear.toString());

        return ResponseEntity.noContent().build();
    }

    /* (참고) 액세스 토큰 유효성 점검 */
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(Authentication authentication) {
        if (authentication == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰");
        return ResponseEntity.ok("인증된 사용자: " + authentication.getName());
    }

    // --- 응답 DTO ---
    public record LoginResponse(String accessToken, long accessTokenExpiresIn, LoginUser user) {}
    public record LoginUser(Long id, String email, String name) {}
    public record AccessTokenResponse(String accessToken, long accessTokenExpiresIn) {}
}

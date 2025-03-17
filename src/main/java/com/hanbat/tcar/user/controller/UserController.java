package com.hanbat.tcar.user.controller;

import com.hanbat.tcar.security.jwt.JWToken;
import com.hanbat.tcar.security.jwt.JwtGenerator;
import com.hanbat.tcar.security.service.RefreshTokenService;
import com.hanbat.tcar.user.UserRepository;
import com.hanbat.tcar.user.service.UserService;
import com.hanbat.tcar.user.dto.*;
import com.hanbat.tcar.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;          // Cookie import
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/users/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtGenerator jwtGenerator;         // Access/Refresh 토큰 생성
    private final RefreshTokenService refreshTokenService; // Redis에 RT 저장
    private final UserRepository userRepository;

    @Tag(name = "signup", description = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<SimpleMessageResponseDto> signUp(@RequestBody UserSignupRequestDto userSignupRequestDto) {
        try {
            userService.signUp(userSignupRequestDto);
            SimpleMessageResponseDto responseDto = SimpleMessageResponseDto.builder()
                    .message("회원가입이 완료되었습니다")
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (ResponseStatusException e) {
            SimpleMessageResponseDto responseDto = SimpleMessageResponseDto.builder()
                    .message(e.getReason())
                    .build();
            return ResponseEntity.status(e.getStatusCode()).body(responseDto);
        }
    }

    @Tag(name = "login", description = "로그인")
    @PostMapping("login")
    public ResponseEntity<?> login(
            @RequestBody UserLoginRequestDto userLoginRequestDto,
            HttpServletResponse response
    ) {
        try {
            // 1) userService.userFind(...)로 아이디/비밀번호 검증
            User user = userService.userFind(userLoginRequestDto);

            // 2) JWT 생성 (Access Token 15분, Refresh Token 7일 등)
            JWToken token = jwtGenerator.generateToken(user);
            String accessToken = token.getAccessToken();
            String refreshToken = token.getRefreshToken();

            // 3) Refresh Token → Redis 저장
            refreshTokenService.storeRefreshToken(user.getId(), refreshToken, 7L);

            // Access Token → 쿠키로 설정 (HttpOnly, Secure 등)
            Cookie cookie = new Cookie("ACCESS_TOKEN", accessToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);   // HTTPS 환경에서만 사용할 때, 로컬 개발시 false로 test, 운영에서는 true로 test
            cookie.setPath("/");
            cookie.setMaxAge(15 * 60); // 15분
            response.addCookie(cookie);

            // 5) Refresh Token도 쿠키 // Refresh Token도 쿠키로 내려주거나, Body로 내려줄 수 있음 (정책에 따라)
            Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(refreshCookie);

            // 6) 정상 로그인 응답
            return ResponseEntity.ok(SimpleMessageResponseDto.builder().message("로그인 성공").build());

        } catch (ResponseStatusException e) {
            // 로그인 과정에서 발생한 예외 처리
            UserLoginFailureResponseDto userLoginFailureResponseDto = UserLoginFailureResponseDto.builder()
                    .message(e.getReason())
                    .build();
            return ResponseEntity.status(e.getStatusCode()).body(userLoginFailureResponseDto);
        }
    }

    /**
     * 토큰이 유효할 경우, SecurityContext 에서 Principal 로 이메일을 꺼내 확인하는 예시
     */
    @GetMapping("/user")
    public ResponseEntity<String> getMyEmail(Authentication authentication) {
        if (authentication == null) {
            // 토큰이 없거나 유효하지 않은 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("인증 실패: 토큰이 없습니다 or 잘못되었습니다.");
        }
        // JwtFilter 에서 principal 에 email 을 넣었다면 여기서 꺼내 쓸 수 있음
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok("인증된 사용자 이메일: " + email);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request,
                                     HttpServletResponse response) {
        // 1) 쿠키에서 refresh token 추출
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No refresh token in cookie");
        }

        // 2) Redis 조회
        Long userId = refreshTokenService.getUserIdByRefreshToken(refreshToken);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid refresh token");
        }

        // 3) 사용자 조회 + 새 Access Token 생성
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found");
        }
        JWToken newToken = jwtGenerator.generateToken(user);
        String newAccessToken = newToken.getAccessToken();

        // 4) Access Token 쿠키 재설정
        Cookie newAccessCookie = new Cookie("ACCESS_TOKEN", newAccessToken);
        newAccessCookie.setHttpOnly(true);
        newAccessCookie.setSecure(true); // 운영환경
        newAccessCookie.setPath("/");
        newAccessCookie.setMaxAge(15 * 60);
        response.addCookie(newAccessCookie);

        return ResponseEntity.ok("새로운 Access Token 발급 완료");
    }

    // 헬퍼 메서드
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("REFRESH_TOKEN".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response) {
        // 1) 쿠키에서 refresh token 추출
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            // 2) Redis에서 해당 토큰 삭제
            refreshTokenService.deleteRefreshToken(refreshToken);

            // 3) 쿠키 만료
            Cookie expireCookie = new Cookie("REFRESH_TOKEN", null);
            expireCookie.setHttpOnly(true);
            expireCookie.setSecure(true);
            expireCookie.setPath("/");
            expireCookie.setMaxAge(0); // 바로 만료
            response.addCookie(expireCookie);
        }

        // Access Token 쿠키도 필요 시 만료 처리
        Cookie expireAccess = new Cookie("ACCESS_TOKEN", null);
        expireAccess.setHttpOnly(true);
        expireAccess.setSecure(true);
        expireAccess.setPath("/");
        expireAccess.setMaxAge(0);
        response.addCookie(expireAccess);

        return ResponseEntity.ok(SimpleMessageResponseDto.builder().message("로그아웃 완료").build());
    }

    @GetMapping("/role")
    public ResponseEntity<UserRoleResponseDto> getUserRole(@RequestParam("email") String email) {
        // 1) DB 조회 (Optional<User>)
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new UserRoleResponseDto("User not found"));
        }

        // 2) 등급 반환
        User user = userOpt.get();
        return ResponseEntity.ok(new UserRoleResponseDto(user.getRole().name()));
    }

}
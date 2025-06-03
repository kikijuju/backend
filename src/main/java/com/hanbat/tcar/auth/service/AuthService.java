package com.hanbat.tcar.auth.service;

import com.hanbat.tcar.auth.jwt.JWToken;
import com.hanbat.tcar.auth.jwt.JwtGenerator;
import com.hanbat.tcar.user.UserRepository;
import com.hanbat.tcar.user.dto.SimpleMessageResponseDto;
import com.hanbat.tcar.user.entity.User;
import com.hanbat.tcar.user.dto.UserLoginRequestDto;
import com.hanbat.tcar.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtGenerator jwtGenerator;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService; // 예: 아이디/비밀번호 검증 시 사용

    // 1) 로그인 로직
    public SimpleMessageResponseDto login(UserLoginRequestDto userLoginRequestDto,
                                          HttpServletResponse response) {
        // 1) userService.userFind(...)로 아이디/비밀번호 검증
        User user = userService.userFind(userLoginRequestDto); // 예: 아이디/비번 맞는지 체크

        // 2) JWT 생성
        JWToken token = jwtGenerator.generateToken(user);
        String accessToken = token.getAccessToken();
        String refreshToken = token.getRefreshToken();

        // 3) Refresh Token → Redis 저장
        refreshTokenService.storeRefreshToken(user.getId(), refreshToken, 7L);

        // 4) Access Token, Refresh Token을 쿠키에 설정 (ResponseCookie 사용)
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", accessToken)
                .httpOnly(false)
                .secure(false)  // 로컬 테스트 시 false, 운영 시 true
                .path("/")
                .maxAge(Duration.ofMinutes(15))
//                .sameSite("None")  // SameSite 설정 추가
                //.secure(true)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(7))
                //.sameSite("None")
                //.secure(true)
                .build();

        response.setHeader("Authorization", "Bearer " + accessToken);
//        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        // 1. javax.servlet.http.Cookie 타입 사용
//        Cookie accessCookie = new Cookie("ACCESS_TOKEN", accessToken);
//        accessCookie.setHttpOnly(true);
//        accessCookie.setSecure(false); // 개발에서는 false, 운영에서는 true
//        accessCookie.setPath("/");
//        accessCookie.setMaxAge(60 * 15); // 15분
//
//        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken);
//        refreshCookie.setHttpOnly(true);
//        refreshCookie.setSecure(false);
//        refreshCookie.setPath("/");
//        refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7일

// 2. 진짜 쿠키 추가
//        response.addCookie(accessCookie);
//        response.addCookie(refreshCookie);




        // 5) 정상 로그인 응답
        return SimpleMessageResponseDto.builder().message("로그인 성공").build();
    }

    // 2) 토큰 갱신(refresh)
    public String refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 1) 쿠키에서 refresh token 추출
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No refresh token in cookie");
        }

        // 2) Redis 조회
        Long userId = refreshTokenService.getUserIdByRefreshToken(refreshToken);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        // 3) 사용자 조회
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        // 4) 새 Access Token 생성
        JWToken newToken = jwtGenerator.generateToken(user);
        String newAccessToken = newToken.getAccessToken();

        // 5) Access Token 쿠키 재설정
        Cookie newAccessCookie = createCookie("ACCESS_TOKEN", newAccessToken, 15 * 60);
        response.addCookie(newAccessCookie);

        return "새로운 Access Token 발급 완료";
    }

    // 3) 로그아웃 로직
    public SimpleMessageResponseDto logout(HttpServletRequest request,
                                           HttpServletResponse response) {
        // 1) 쿠키에서 refresh token 추출
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            // 2) Redis에서 해당 토큰 삭제
            refreshTokenService.deleteRefreshToken(refreshToken);

            // 3) 쿠키 만료
            Cookie expireCookie = createCookie("REFRESH_TOKEN", null, 0);
            response.addCookie(expireCookie);
        }

        // Access Token 쿠키도 필요 시 만료 처리
        Cookie expireAccess = createCookie("ACCESS_TOKEN", null, 0);
        response.addCookie(expireAccess);

        return SimpleMessageResponseDto.builder().message("로그아웃 완료").build();
    }

    // 쿠키 생성 헬퍼 메서드
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 로컬 테스트 시 false, 운영 시 true
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    // Refresh Token 쿠키 추출 헬퍼 메서드
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
}
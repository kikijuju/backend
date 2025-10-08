package com.hanbat.tcar.user.controller;

import com.hanbat.tcar.auth.jwt.JWToken;
import com.hanbat.tcar.user.UserRepository;
import com.hanbat.tcar.user.dto.*;
import com.hanbat.tcar.user.entity.User;
import com.hanbat.tcar.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.hanbat.tcar.auth.service.AuthService;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/users/")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;
    private final UserRepository userRepository;

    @Tag(name = "signup", description = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<SimpleMessageResponseDto> signUp(@RequestBody UserSignupRequestDto userSignupRequestDto) {
        log.info("회원가입 요청 시작: 이메일 = {}", userSignupRequestDto.getEmail());
        try {
            userService.signUp(userSignupRequestDto);
            log.info("회원가입 성공: 이메일 = {}", userSignupRequestDto.getEmail());
            SimpleMessageResponseDto responseDto = SimpleMessageResponseDto.builder()
                    .message("회원가입이 완료되었습니다")
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (ResponseStatusException e) {
            log.error("회원가입 실패: 이메일 = {}, 에러 메시지 = {}", userSignupRequestDto.getEmail(), e.getReason());
            SimpleMessageResponseDto responseDto = SimpleMessageResponseDto.builder()
                    .message(e.getReason())
                    .build();
            return ResponseEntity.status(e.getStatusCode()).body(responseDto);
        }
    }

    /*────────────────────────── 로그인 (AccessToken 전용) ─────────────────────*/
    @PostMapping("/login")
    public ResponseEntity<JWToken> login(
            @RequestBody UserLoginRequestDto userLoginRequestDto) {

        log.info("로그인 요청: {}", userLoginRequestDto.getEmail());

        // AuthService.login() → accessToken만 담긴 JWToken 반환
        JWToken tokens = authService.login(userLoginRequestDto);

        log.info("로그인 성공: accessToken 만료 7일");

        // 1) 헤더엔 오직 accessToken 문자열만
        String bearer = "Bearer " + tokens.getAccessToken();

        // 2) Body엔 필요하면 refreshToken만 내려줄 수도 있고, 아니면 JWToken 전체
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .body(tokens);
    }

    /*──────────────────────────────── 로그아웃 ───────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<SimpleMessageResponseDto> logout(
            @RequestHeader("Refresh-Token") String refreshToken) { // ★ 헤더로 전달

        authService.logout(refreshToken);

        return ResponseEntity.ok(
                new SimpleMessageResponseDto("로그아웃 완료"));
    }
    */

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
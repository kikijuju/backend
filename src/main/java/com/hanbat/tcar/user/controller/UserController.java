package com.hanbat.tcar.user.controller;

import com.hanbat.tcar.user.UserRepository;
import com.hanbat.tcar.user.dto.*;
import com.hanbat.tcar.user.entity.User;
import com.hanbat.tcar.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDto userLoginRequestDto,
                                   HttpServletResponse response) {
        // 로그인 요청 전 로그 출력
        log.info("로그인 요청 시작: 사용자 이메일 = {}", userLoginRequestDto.getEmail());
        // AuthService에 로그인 로직 위임
        SimpleMessageResponseDto result = authService.login(userLoginRequestDto, response);
        // 로그인 결과 로그 출력
        log.info("로그인 요청 완료: 결과 메시지 = {}", result.getMessage());
        return ResponseEntity.ok(result);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response) {
        SimpleMessageResponseDto result = authService.logout(request, response);
        return ResponseEntity.ok(result);
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
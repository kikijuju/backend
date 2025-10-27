package com.hanbat.tcar.user.controller;

import com.hanbat.tcar.user.UserRepository;
import com.hanbat.tcar.user.dto.*;
import com.hanbat.tcar.user.entity.User;
import com.hanbat.tcar.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Tag(name = "signup", description = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<SimpleMessageResponseDto> signUp(@RequestBody UserSignupRequestDto req) {
        log.info("회원가입 요청 시작: 이메일 = {}", req.getEmail());
        try {
            userService.signUp(req);
            log.info("회원가입 성공: 이메일 = {}", req.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(SimpleMessageResponseDto.builder().message("회원가입이 완료되었습니다").build());
        } catch (ResponseStatusException e) {
            log.error("회원가입 실패: 이메일 = {}, 에러 메시지 = {}", req.getEmail(), e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                    .body(SimpleMessageResponseDto.builder().message(e.getReason()).build());
        }
    }

    @GetMapping("/role")
    public ResponseEntity<UserRoleResponseDto> getUserRole(@RequestParam("email") String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new UserRoleResponseDto("User not found"));
        }
        return ResponseEntity.ok(new UserRoleResponseDto(userOpt.get().getRole().name()));
    }
}

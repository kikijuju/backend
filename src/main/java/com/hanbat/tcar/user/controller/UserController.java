package com.hanbat.tcar.user.controller;

import com.hanbat.tcar.user.UserRepository;
import com.hanbat.tcar.user.dto.*;
import com.hanbat.tcar.user.entity.User;
import com.hanbat.tcar.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
        return ResponseEntity.ok(new UserRoleResponseDto(userOpt.get().getTier().name()));
    }

    @GetMapping("/me")
    @Operation(summary = "현재 로그인한 사용자 정보 조회",
            description = "토큰에 포함된 이메일 기반으로 사용자 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<UserInfoResponseDto> getMyInfo(Authentication auth) {
        String email = (String) auth.getPrincipal();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(new UserInfoResponseDto(user));
    }

}

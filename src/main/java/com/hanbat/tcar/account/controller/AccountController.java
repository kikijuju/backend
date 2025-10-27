package com.hanbat.tcar.account.controller;

import com.hanbat.tcar.account.dto.PasswordChangeRequestDto;
import com.hanbat.tcar.user.service.UserService;
import com.hanbat.tcar.account.dto.AccountUpdateRequestDto;
import com.hanbat.tcar.account.service.AccountService;
import com.hanbat.tcar.user.dto.SimpleMessageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/myaccount")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    @Operation(summary = "회원 정보 수정", description = "닉네임, 이름, 생년월일, 성별 수정")
    @PutMapping
    public ResponseEntity<SimpleMessageResponseDto> updateAccountInfo(
            Authentication authentication,
            @RequestBody @Valid AccountUpdateRequestDto request
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(SimpleMessageResponseDto.builder()
                            .message("인증되지 않은 사용자입니다.")
                            .build());
        }

        String email = (String) authentication.getPrincipal();
        accountService.updateUserInfo(email, request);

        return ResponseEntity.ok(SimpleMessageResponseDto.builder()
                .message("회원 정보가 성공적으로 수정되었습니다.")
                .build());
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임이 이미 존재하는지 확인")
    @GetMapping("/nickname/check")
    public ResponseEntity<SimpleMessageResponseDto> checkNicknameDuplicate(@RequestParam("nickname") String nickname) {
        boolean exists = userService.isNicknameDuplicate(nickname);

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(SimpleMessageResponseDto.builder()
                            .message("이미 사용 중인 닉네임입니다.")
                            .build());
        } else {
            return ResponseEntity.ok(SimpleMessageResponseDto.builder()
                    .message("사용 가능한 닉네임입니다.")
                    .build());
        }
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인된 사용자의 계정 삭제")
    @DeleteMapping("/deactivate")
    public ResponseEntity<SimpleMessageResponseDto> deactivateUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(SimpleMessageResponseDto.builder()
                            .message("인증되지 않은 사용자입니다.")
                            .build());
        }

        String email = (String) authentication.getPrincipal();
        accountService.deleteUser(email);

        return ResponseEntity.ok(SimpleMessageResponseDto.builder()
                .message("회원 탈퇴가 완료되었습니다.")
                .build());
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인하고 새 비밀번호로 변경합니다.")
    @PutMapping("/reset_password")
    public ResponseEntity<SimpleMessageResponseDto> changePassword(
            Authentication authentication,
            @RequestBody @Valid PasswordChangeRequestDto requestDto) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(SimpleMessageResponseDto.builder().message("인증되지 않은 사용자입니다.").build());
        }

        String email = (String) authentication.getPrincipal();
        accountService.changePassword(email, requestDto);

        return ResponseEntity.ok(SimpleMessageResponseDto.builder().message("비밀번호가 성공적으로 변경되었습니다.").build());
    }
}
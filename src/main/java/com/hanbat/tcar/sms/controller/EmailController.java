package com.hanbat.tcar.sms.controller;

import com.hanbat.tcar.sms.dto.EmailSendRequest;
import com.hanbat.tcar.sms.dto.EmailVerifyRequest;
import com.hanbat.tcar.sms.dto.SingleResponseDto;
import com.hanbat.tcar.sms.service.EmailVerificationService;
import com.hanbat.tcar.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailVerificationService emailVerificationService;
    private final UserService userService;
    //1) 이메일 인증번호 전송 요청
    @PostMapping("/send")
    public ResponseEntity<SingleResponseDto<String>> sendVerificationCode(
            @RequestBody @Valid EmailSendRequest request) {
        // 이메일 중복 체크
        userService.checkDuplicatedEmail(request.getEmail());
        emailVerificationService.sendCodeToEmail(request.getEmail());
        return new ResponseEntity<>(new SingleResponseDto<>("인증번호 전송 완료."), HttpStatus.OK);
    }

     //2) 이메일 인증번호 검증 요청
     @PostMapping("/verify")
     public ResponseEntity<SingleResponseDto<String>> verifyEmail(
             @RequestBody @Valid EmailVerifyRequest request) {
         String message = emailVerificationService.getVerificationMessage(request.getEmail(), request.getCode());
         return new ResponseEntity<>(new SingleResponseDto<>(message), HttpStatus.OK);
     }
}
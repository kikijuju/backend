package com.hanbat.tcar.sms.controller;

import com.hanbat.tcar.sms.dto.SingleResponseDto;
import com.hanbat.tcar.sms.dto.SmsRequest;
import com.hanbat.tcar.sms.dto.SmsVerifyRequest;
import com.hanbat.tcar.sms.service.SmsUtil;
import com.hanbat.tcar.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsUtil smsUtil;
    private final UserService userService;


    // SMS 인증번호 전송
    @PostMapping("/send")
    public ResponseEntity<SingleResponseDto<String>> sendSms(@RequestBody SmsRequest smsRequest) {
        try {
            userService.checkDuplicatedPhoneNumber(smsRequest.getPhoneNumber());
            smsUtil.sendCertificationSMS(smsRequest.getPhoneNumber());
            return new ResponseEntity<>(new SingleResponseDto<>("인증번호 전송 완료"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new SingleResponseDto<>("인증번호 전송 실패: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // SMS 인증번호 검증
    @PostMapping("/verify")
    public ResponseEntity<String> verifySms(@RequestBody SmsVerifyRequest smsVerifyRequest) {
        try {
            boolean verified = smsUtil.verifyCertificationCode(
                    smsVerifyRequest.getPhoneNumber(),
                    smsVerifyRequest.getCode());
            if (verified) {
                return ResponseEntity.ok("인증 성공");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("인증 실패: 인증번호가 일치하지 않습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("인증 실패: " + e.getMessage());
        }
    }
}
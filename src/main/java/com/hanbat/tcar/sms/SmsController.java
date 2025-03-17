package com.hanbat.tcar.sms;

import com.hanbat.tcar.sms.dto.SmsRequest;
import com.hanbat.tcar.sms.dto.SmsVerifyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsUtil smsUtil;

    /**
     * 프론트엔드에서 전화번호를 받아 SMS 인증번호를 전송하는 API
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendSms(@RequestBody SmsRequest smsRequest) {
        try {
            smsUtil.sendCertificationSMS(smsRequest.getPhoneNumber());
            return ResponseEntity.ok("인증번호 전송 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("인증번호 전송 실패: " + e.getMessage());
        }
    }

    /**
     * 프론트엔드에서 전화번호와 입력한 인증번호를 받아 검증하는 API
     */
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
                        .body("인증 실패: 코드가 일치하지 않습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("인증 실패: " + e.getMessage());
        }
    }
}
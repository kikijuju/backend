package com.hanbat.tcar.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value; // @Value
import org.springframework.transaction.annotation.Transactional; // @Transactional

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.time.Duration;

import com.hanbat.tcar.sms.dto.EmailVerificationResult;
import com.hanbat.tcar.sms.exception.BusinessLogicException;
import com.hanbat.tcar.sms.exception.ExceptionCode;


// 인증 번호 생성 검증을 담당할 클래스
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String AUTH_CODE_PREFIX = "AuthCode ";
    private final MailService mailService;
    private final RedisUtil redisUtil;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private long authCodeExpirationMillis;

    public void sendCodeToEmail(String toEmail) {
        String title = "Service 이메일 인증 번호";
        String authCode = this.createCode();
        mailService.sendEmail(toEmail, title, authCode);
        // 이메일 인증 요청 시 인증 번호 Redis에 저장 ( key = "AuthCode " + Email / value = AuthCode )
        redisUtil.set(AUTH_CODE_PREFIX + toEmail,
                authCode, Duration.ofMillis(this.authCodeExpirationMillis));
    }


    private String createCode() {
        int length = 6;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            log.debug("MemberService.createCode() exception occur");
            throw new BusinessLogicException(ExceptionCode.NO_SUCH_ALGORITHM);
        }
    }

    public EmailVerificationResult verifiedCode(String email, String authCode) {
        String redisAuthCode = redisUtil.get(AUTH_CODE_PREFIX + email);
        boolean authResult = redisUtil.checkExistsValue(redisAuthCode) && redisAuthCode.equals(authCode);

        return EmailVerificationResult.of(authResult);
    }

    public String getVerificationMessage(String email, String code) {
        EmailVerificationResult result = verifiedCode(email, code);
        return result.isVerified()
                ? "이메일 검증이 완료되었습니다."
                : "인증번호가 올바르지 않습니다.";
    }
}

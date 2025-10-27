package com.hanbat.tcar.sms.service;

import com.hanbat.tcar.sms.exception.SmsSendException;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.model.MessageType;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class SmsUtil {

    private final RedisUtil redisUtil;
    private static final Logger logger = LoggerFactory.getLogger(SmsUtil.class);

    @Value("${coolsms.api.key}")
    private String apiKey;
    @Value("${coolsms.api.secret}")
    private String apiSecretKey;
    @Value("${coolsms.api.fromnum}")
    private String fromNumber;




    // CoolSMS 전송 서비스
    private DefaultMessageService messageService;

    /**
     * 스프링이 빈을 생성한 직후(의존성 주입 완료) 실행되는 메서드
     * 여기서 NurigoApp.INSTANCE.initialize(...)로 초기화
     */
    @PostConstruct
    public void init() {
        messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecretKey, "https://api.coolsms.co.kr");
    }

    /**
     * 6자리 인증번호 생성
     */
    private String createRandomNumber() {
        Random rand = new Random();
        StringBuilder randomNum = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            randomNum.append(rand.nextInt(10));
        }
        return randomNum.toString();
    }

    /**
     * 전화번호로 인증 SMS 전송
     * 1. 전화번호 유효성 검증
     * 2. 6자리 인증번호 생성 → Redis에 저장(만료: 3분)
     * 3. CoolSMS API 이용해 실제 문자 전송
     */
    @Transactional
    public void sendCertificationSMS(String phoneNumber) {
        // 1) 유효성 체크
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new SmsSendException("유효하지 않은 전화번호입니다.");
        }

        // 2) 인증번호 생성
        String randomNum = createRandomNumber();

        // 3) Redis에 저장 (3분 만료)
        redisUtil.set(phoneNumber, randomNum, 3);

        // 4) Message 객체 생성 후 값 세팅
        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(phoneNumber);
        message.setText("[Service] 인증번호 [" + randomNum + "]를 입력해 주세요.");
        message.setType(MessageType.valueOf("SMS"));

        // 5) Request 생성 및 전송
        SingleMessageSendingRequest request = new SingleMessageSendingRequest(message);

        try {
            SingleMessageSentResponse response = messageService.sendOne(request);
            logger.info("SMS 전송 성공: {}", response);
        } catch (Exception e) {
            logger.error("SMS 전송 실패", e);
            throw new SmsSendException("SMS 전송에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자가 입력한 인증번호를 검증하는 메서드
     * 1. Redis에서 해당 전화번호로 저장된 인증번호 조회
     * 2. 입력한 인증번호와 비교
     * 3. 일치하면 Redis에서 삭제하고 true 반환, 불일치하면 false 반환
     */
    public boolean verifyCertificationCode(String phoneNumber, String inputCode) {
        String savedCode = redisUtil.get(phoneNumber);
        if (savedCode == null) {
            throw new SmsSendException("인증번호가 만료되었거나 존재하지 않습니다.");
        }
        if (savedCode.equals(inputCode)) {
            redisUtil.delete(phoneNumber);
            return true;
        } else {
            return false;
        }
    }
}
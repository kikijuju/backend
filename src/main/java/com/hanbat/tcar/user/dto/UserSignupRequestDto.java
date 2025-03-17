package com.hanbat.tcar.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignupRequestDto {
    private String email; //유저 이메일(아이디)
    private String password; //비밀번호
    private String confirmPassword;//비밀번호 확인
    private String username; //사용자 이름
    private String nickname; // 사용자 닉네임
    private String phoneNumber; // 전화번호
    private String verificationCode; // 인증코드

}

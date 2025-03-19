package com.hanbat.tcar.user.dto;

import com.hanbat.tcar.user.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    private String phoneNumber;     // 추가
    private LocalDate birthDate;      // 추가 (예: "1990-01-01" 형태의 문자열을 LocalDate로 변환할 수 있도록 컨버터 설정)
    private Gender gender;


}

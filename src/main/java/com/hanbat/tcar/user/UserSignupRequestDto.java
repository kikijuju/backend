package com.hanbat.tcar.user;

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
    private String username; //사용자 이름
    private String nickname; // 사용자 닉네임

}

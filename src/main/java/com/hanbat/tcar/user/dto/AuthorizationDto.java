package com.hanbat.tcar.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizationDto {
    private String message; //서버의 메시지 (ex - 회원가입이 완료되었습니다)
    private String token;

}



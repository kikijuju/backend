package com.hanbat.tcar.auth.jwt;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class JWToken {
    private String accessToken;
    private long accessTokenExpiresIn; // ★ 추가 (초 단위)
    private String refreshToken;
}

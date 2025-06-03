package com.hanbat.tcar.auth.jwt;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class JWToken {
    private String accessToken;
    private String refreshToken;
}

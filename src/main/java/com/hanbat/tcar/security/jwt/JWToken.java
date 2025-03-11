package com.hanbat.tcar.security.jwt;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class JWToken {
    private String accessToken;
    private String refreshToken;
}

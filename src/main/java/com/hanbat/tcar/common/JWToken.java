package com.hanbat.tcar.common;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class JWToken {
    private String accessToken;
    private String refreshToken;
}

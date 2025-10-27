package com.hanbat.tcar.auth.service;

public interface RefreshTokenService {
    void storeRefreshToken(Long userId, String refreshToken, long daysTtl);
    Long getUserIdByRefreshToken(String refreshToken);
    void deleteRefreshToken(String refreshToken);
    void deleteRefreshTokenForUser(Long userId);
    long getTtlSeconds(String refreshToken);

    // (선택) 안전한 로테이션 헬퍼
    void rotateRefreshToken(Long userId, String oldRefreshToken, String newRefreshToken, long daysTtl);
}

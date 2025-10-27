package com.hanbat.tcar.sms.dto;

public class EmailVerificationResult {
    private final boolean verified;

    private EmailVerificationResult(boolean verified) {
        this.verified = verified;
    }

    public static EmailVerificationResult of(boolean verified) {
        return new EmailVerificationResult(verified);
    }

    public boolean isVerified() {
        return verified;
    }
}

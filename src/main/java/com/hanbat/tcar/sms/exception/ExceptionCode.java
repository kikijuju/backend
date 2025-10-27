package com.hanbat.tcar.sms.exception;

public enum ExceptionCode {
    UNABLE_TO_SEND_EMAIL("E001", "이메일 전송에 실패했습니다."),
    INVALID_INPUT("E002", "입력값이 유효하지 않습니다."),
    NO_SUCH_ALGORITHM("E003", "알고리즘이 존재하지 않습니다.");
    private final String code;
    private final String message;

    ExceptionCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
    }
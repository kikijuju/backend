package com.hanbat.tcar.sms.exception;

public class BusinessLogicException extends RuntimeException {

    private final ExceptionCode code;

    public BusinessLogicException(ExceptionCode code) {
        super(code.getMessage()); // 부모 클래스(RunTimeException)에 메시지 전달
        this.code = code;
    }

    public ExceptionCode getCode() {
        return code;
    }
}

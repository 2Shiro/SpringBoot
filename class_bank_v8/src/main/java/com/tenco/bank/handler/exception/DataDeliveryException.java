package com.tenco.bank.handler.exception;
// 사용자 정의 예외 클래스 만들기

import org.springframework.http.HttpStatus;

public class DataDeliveryException extends RuntimeException {

    private HttpStatus status;
    // 예외 발생했을 때 --> Http 상태코드를 알려 준다.
    // 메세지 (어떤 예외 발생)
    public DataDeliveryException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}


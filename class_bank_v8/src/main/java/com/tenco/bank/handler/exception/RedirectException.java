// 사용자 정의 예외 클래스 만들기
package com.tenco.bank.handler.exception;

import org.springframework.http.HttpStatus;

public class RedirectException extends RuntimeException {

    private HttpStatus status;
    // 예외가 발생했을 때 --> Http 상태코드를 알려준다
    // 메세지 (어떤 예외 발생)
    public RedirectException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
package com.tenco.class_jwt.domain;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    // accessToken DB에 저장 X
    // DB 저장, 엑세스 토큰이 만료되었을 때 새 엑세스 토큰 발급을 위해 사용(7일)
    private String refreshToken;
}

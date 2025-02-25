package com.tenco.class_jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {

    // 엑세스 토큰
    private String accessToken;
    // 리프레시 토큰
    private String refreshToken;
    // 사용자 이름
    private String username;
    // 나머지 추가적인 정보가 필요하다면 토큰을 확인하고 별도에 API로 제공한다

}

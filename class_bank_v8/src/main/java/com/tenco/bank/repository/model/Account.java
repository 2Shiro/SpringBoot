package com.tenco.bank.repository.model;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Account Entity 를 설계 중입니다.
// Enitity 로 사용하는 클래스는 로직을 포함 할 수 있다.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    private Integer id;
    private String number;
    private String password;
    private Long balance;
    private Integer userId;
    private Timestamp createdAt;

    // 출금 기능
    public void withdraw(Long amount) {
        // 방어적 코드 작성 예정
        this.balance -= amount;
    }
    // 입금 기능
    public void deposit(Long amount) {
        this.balance += amount;
    }

    // TODO - 추후 추가
    // 패스워드 체크 기능
    // 잔액 여부 확인 기능
    // 계좌 소유자 확인 기능

}
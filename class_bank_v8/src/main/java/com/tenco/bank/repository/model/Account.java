package com.tenco.bank.repository.model;

import java.sql.Timestamp;
import java.text.DecimalFormat;

import com.tenco.bank.handler.exception.DataDeliveryException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;


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
    public boolean checkPassword(String password) {
        boolean isOk = true;
        if(this.password.equals(password) == false) {
           // 사용자 한테 비밀 틀렸어요
            isOk = false;
            throw new DataDeliveryException("계좌 비밀번호가 틀렸어요", HttpStatus.BAD_REQUEST);
        }
        return isOk;
    }
    // 잔액 여부 확인 기능 500 / 500
    public void checkBalance(Long amount) {
        if(this.balance < amount) {
            throw new DataDeliveryException("잔액이 부족합니다", HttpStatus.BAD_REQUEST);
        }
    }
    // 계좌 소유자 확인 기능
    public void checkOwner(Integer principalId) {
        if(this.userId != principalId) {
            throw new DataDeliveryException("본인 계좌가 아닙니다", HttpStatus.BAD_REQUEST);
        }
    }

    // 화폐 형식으로 변환
    public String formatKoreanWon(Long amount) {
        DecimalFormat formatter = new DecimalFormat("#,###원");
        return formatter.format(amount);
    }

}
package com.tenco.bank.dto;

import lombok.Data;

@Data
public class TransferDTO {
    private Long amount;
    private String wAccountNumber;
    private String dAccountNumber;
    private String password; // 출금 계좌 비밀번호
}
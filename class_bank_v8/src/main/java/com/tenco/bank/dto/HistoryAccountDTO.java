package com.tenco.bank.dto;

import lombok.Data;

import java.sql.Timestamp;

// JOIN 결과를 매핑할 모델은 DTO로 설계하는 일반적이다.
@Data
public class HistoryAccountDTO {
    private Integer id;
    private Long amount;
    private Long balance;
    private String sender;
    private String receiver;
    private Timestamp createdAt;
}

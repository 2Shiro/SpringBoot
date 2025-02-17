package com.tenco.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferDTO {
    private Long amount;
    private String wAccountNumber;
    private String dAccountNumber;
    private String password;
}
package com.obs.Online_Banking_System.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.obs.Online_Banking_System.entity.Account;
import com.obs.Online_Banking_System.enumDto.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {

    private Long transactionId;

    private TransactionType transactionType;

    private BigDecimal amount;

    private Instant timestamp;

    // User remark
    private String remark;

    private BigDecimal remainingBalance;

    private Account account;

    // For transfer
    private Long targetAccountNumber;

    private Long senderAccountId;

    private Long receiverAccountId;
}

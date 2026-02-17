package com.obs.Online_Banking_System.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.obs.Online_Banking_System.enumDto.AccountType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    
    private Long id;

    private Long accountNumber;

    private AccountType accountType;

    private BigDecimal balance;

    private Long adharcard;

    private Instant createdAt;

    private String branch;

    private String ifsc;
}

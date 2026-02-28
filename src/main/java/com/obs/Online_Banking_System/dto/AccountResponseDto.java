package com.obs.Online_Banking_System.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.obs.Online_Banking_System.enumDto.AccountType;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class AccountResponseDto {

    private Long id;

    private Long accountNumber;

    private AccountType accountType;

    private BigDecimal balance;

    private Long adharcard;

    private Instant createdAt;

    private String branch;

    private String ifsc;

    private CustomerDto customerDto;
}

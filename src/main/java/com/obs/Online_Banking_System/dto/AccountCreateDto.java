package com.obs.Online_Banking_System.dto;

import com.obs.Online_Banking_System.enumDto.AccountType;

import lombok.Data;

@Data
public class AccountCreateDto {
    private AccountType accountType;
    private String email;
}

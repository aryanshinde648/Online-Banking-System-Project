package com.obs.Online_Banking_System.service;


import com.obs.Online_Banking_System.dto.AccountCreateDto;
import com.obs.Online_Banking_System.dto.AccountDto;

public interface AccountService {
    AccountDto createAccount(AccountCreateDto accountCreateDto);

    AccountDto getAccountById(Long id);

    String deleteAccount(Long id);

    AccountDto getAccountByAccountNumber(Long accountNumber);

    AccountDto getAccountByUserEmail(String email);
}

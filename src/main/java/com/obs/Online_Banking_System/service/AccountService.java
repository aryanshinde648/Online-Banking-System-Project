package com.obs.Online_Banking_System.service;


import java.util.List;

import com.obs.Online_Banking_System.dto.AccountCreateDto;
import com.obs.Online_Banking_System.dto.AccountDto;
import com.obs.Online_Banking_System.dto.AccountResponseDto;

public interface AccountService {
    AccountDto createAccount(AccountCreateDto accountCreateDto);

    AccountDto getAccountById(Long id);

    String deleteAccount(Long id);

    AccountDto getAccountByAccountNumber(Long accountNumber);

    AccountDto getAccountByCustomerEmail(String email);

    AccountResponseDto getmyAccount(String email);

    List<AccountDto> getAllAccounts();
}

package com.obs.Online_Banking_System.service.impl;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.obs.Online_Banking_System.dto.AccountCreateDto;
import com.obs.Online_Banking_System.dto.AccountDto;
import com.obs.Online_Banking_System.dto.CustomerDto;
import com.obs.Online_Banking_System.entity.Account;
import com.obs.Online_Banking_System.entity.Customer;
import com.obs.Online_Banking_System.enumDto.AccountType;
import com.obs.Online_Banking_System.mapper.AccountConversion;
import com.obs.Online_Banking_System.repository.AccountRepository;
import com.obs.Online_Banking_System.repository.CustomerRepository;
import com.obs.Online_Banking_System.service.AccountService;
import com.obs.Online_Banking_System.service.CustomerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{

    @Autowired
    private AccountConversion accountConversion;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    private static long ACCOUNT_START = 1000000000L;

    private long generateAccountNumber() {
        return ACCOUNT_START + System.currentTimeMillis() % 1_000_000_000;
    }

    @Override
    public AccountDto createAccount(AccountCreateDto accountCreateDto) {

        Customer customer = customerRepository.findByEmail(accountCreateDto.getEmail());
        if (customer == null) {
            throw new RuntimeException("Customer not found with email "+accountCreateDto.getEmail());
        }

        BigDecimal initialBalance = accountCreateDto.getInitialDeposit() == null
                ? BigDecimal.ZERO
                : new BigDecimal(accountCreateDto.getInitialDeposit());

        long accountNumber = generateAccountNumber();

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(initialBalance);
        account.setAdharcard(customer.getAdharcard());
        account.setCreatedAt(Instant.now());
        account.setAccountType(accountCreateDto.getAccountType());

        accountRepository.save(account);
        AccountDto accountDto = accountConversion.toAccountDto(account);

        return accountDto;
    }

    @Override
    public AccountDto getAccountById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new RuntimeException("Account not found"));
        return accountConversion.toAccountDto(account);
    }

    @Override
    public String deleteAccount(Long id) {

        Account account = accountRepository.findById(id).orElseThrow(() -> new RuntimeException("Account not found"));

        accountRepository.delete(account);

        return "Account deleted successfully";
    }

    @Override
    public AccountDto getAccountByAccountNumber(Long accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new RuntimeException("Account not found with account number: " + accountNumber);
        }
        return accountConversion.toAccountDto(account);
    }

    @Override
    public AccountDto getAccountByUserEmail(String email) {

        /*   Logic
            1. Find the customer by email
            2. Get the adharcard number from the customer
            3. Find the account by adharcard number
        */

        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) {
            throw new RuntimeException("Customer not found with email: " + email);
        }

        Account account = accountRepository.findByAdharcard(customer.getAdharcard());
        if (account == null) {
            throw new RuntimeException("Account not found for customer with email: " + email);
        }

        return accountConversion.toAccountDto(account);
    }
    
}

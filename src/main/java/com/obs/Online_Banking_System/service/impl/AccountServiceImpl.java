package com.obs.Online_Banking_System.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.obs.Online_Banking_System.dto.AccountCreateDto;
import com.obs.Online_Banking_System.dto.AccountDto;
import com.obs.Online_Banking_System.dto.AccountResponseDto;
import com.obs.Online_Banking_System.entity.Account;
import com.obs.Online_Banking_System.entity.Customer;
import com.obs.Online_Banking_System.mapper.AccountConversion;
import com.obs.Online_Banking_System.mapper.CustomerConversion;
import com.obs.Online_Banking_System.repository.AccountRepository;
import com.obs.Online_Banking_System.repository.CustomerRepository;
import com.obs.Online_Banking_System.service.AccountService;
import com.obs.Online_Banking_System.service.CustomerService;
import com.obs.Online_Banking_System.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountConversion accountConversion;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerConversion customerConversion;

    private static long ACCOUNT_START = 1000000000L;

    private long generateAccountNumber() {
        return ACCOUNT_START + System.currentTimeMillis() % 1_000_000_000;
    }

    @Override
    public AccountDto createAccount(AccountCreateDto accountCreateDto) {

        if (customerRepository.findByEmail(accountCreateDto.getEmail()).isEmpty()) {
            throw new RuntimeException("Customer not found with email " + accountCreateDto.getEmail());
        }

        Customer customer = customerRepository.getByEmail(accountCreateDto.getEmail());

        long accountNumber = generateAccountNumber();

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(BigDecimal.ZERO);
        account.setAdharcard(customer.getAdharcard());
        account.setCreatedAt(Instant.now());
        account.setAccountType(accountCreateDto.getAccountType());
        account.setCustomer(customer);

        accountRepository.save(account);
        AccountDto accountDto = accountConversion.toAccountDto(account);

        transactionService.saveInitialDepositTransaction(accountCreateDto, accountDto, customer.getEmail());

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

        if (accountRepository.findByAccountNumber(accountNumber).isEmpty()) {
            throw new RuntimeException("Account not found with account number: " + accountNumber);
        }

        Account account = accountRepository.getByAccountNumber(accountNumber);

        return accountConversion.toAccountDto(account);
    }

    @Override
    public AccountDto getAccountByCustomerEmail(String email) {

        /*
         * Logic
         * 1. Find the customer by email
         * 2. Get the adharcard number from the customer
         * 3. Find the account by adharcard number
         */

        if (customerRepository.findByEmail(email).isEmpty()) {
            throw new RuntimeException("Customer not found with email: " + email);
        }

        Customer customer = customerRepository.getByEmail(email);

        if (accountRepository.findByAdharcard(customer.getAdharcard()).isEmpty()) {
            throw new RuntimeException("Account not found for customer with email: " + email);
        }

        Account account = accountRepository.getByAdharcard(customer.getAdharcard());

        return accountConversion.toAccountDto(account);
    }

    @Override
    public AccountResponseDto getmyAccount(String email) {
        AccountDto acc = getAccountByCustomerEmail(email);

        AccountResponseDto responseDto = AccountResponseDto.builder()
                .id(acc.getAccountId())
                .accountNumber(acc.getAccountNumber())
                .accountType(acc.getAccountType())
                .adharcard(acc.getAdharcard())
                .balance(acc.getBalance())
                .branch(acc.getBranch())
                .ifsc(acc.getIfsc())
                .customerDto(customerConversion.toCustomerDto(acc.getCustomer()))
                .build();

        return responseDto;
    }

    @Override
    public List<AccountDto> getAllAccounts() {
        List<AccountDto> accList = accountConversion.toAccountDtoList(accountRepository.findAll());
        return accList;
    }

}

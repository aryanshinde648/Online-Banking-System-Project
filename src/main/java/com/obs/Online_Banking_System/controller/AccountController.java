package com.obs.Online_Banking_System.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.obs.Online_Banking_System.dto.AccountDto;
import com.obs.Online_Banking_System.dto.AccountResponseDto;
import com.obs.Online_Banking_System.dto.CustomerDto;
import com.obs.Online_Banking_System.service.AccountService;
import com.obs.Online_Banking_System.service.CustomerService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerService customerService;

    // View My Account
    @GetMapping("/me")
    public ResponseEntity<AccountResponseDto> getMyAccount(HttpSession session) {

        CustomerDto cust = (CustomerDto) session.getAttribute("loggedInCustomer");

        AccountDto acc = accountService.getAccountByCustomerEmail(cust.getEmail());

        AccountResponseDto accountResponseDto = AccountResponseDto.builder()
                .accountNumber(acc.getAccountNumber())
                .accountType(acc.getAccountType())
                .adharcard(acc.getAdharcard())
                .balance(acc.getBalance())
                .branch(acc.getBranch())
                .ifsc(acc.getIfsc())
                .customerDto(cust)
                .createdAt(acc.getCreatedAt())
                .build();

        return ResponseEntity.ok(accountResponseDto);
    }

}

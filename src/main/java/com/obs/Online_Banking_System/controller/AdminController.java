package com.obs.Online_Banking_System.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.obs.Online_Banking_System.dto.AccountCreateDto;
import com.obs.Online_Banking_System.dto.AccountDto;
import com.obs.Online_Banking_System.dto.AdminDto;
import com.obs.Online_Banking_System.dto.CustomerDto;
import com.obs.Online_Banking_System.service.AccountService;
import com.obs.Online_Banking_System.service.AdminService;
import com.obs.Online_Banking_System.service.CustomerService;

import lombok.extern.slf4j.Slf4j;


@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/registerAdmin")
    public ResponseEntity<String> registerAdmin(@RequestBody AdminDto adminDto) {
        ResponseEntity<String> msg = adminService.registerAdmin(adminDto);
        log.info(msg.getBody());
        return ResponseEntity.ok(msg.getBody());
    }

    @GetMapping("/demo")
    public ResponseEntity<String> demo() {
        return ResponseEntity.ok("Hello Admin");
    }

    @GetMapping("/getAdminByEmail")
    public ResponseEntity<AdminDto> getAdminByEmail(@RequestHeader String email) {
        log.info("Received request to get admin by email: {}", email);
        return adminService.getAdminByEmail(email);
    }
    
    @GetMapping("/getAccountByAccountNumber")
    public ResponseEntity<AccountDto> getAccountByAccountNumber(@RequestHeader Long accountNumber) {
        AccountDto accountDto = accountService.getAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(accountDto);
    }
    
    @GetMapping("/registerCustomer")
    public ResponseEntity<CustomerDto> registerCustomer(@RequestBody CustomerDto customerDto) {
        CustomerDto cust = customerService.registerCustomer(customerDto);
        return ResponseEntity.ok(cust);
    }

    @GetMapping("/getCustomerByEmail")
    public ResponseEntity<CustomerDto> getCustomerByEmail(@RequestHeader String email) {
        CustomerDto cust = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(cust);
    }
    
    @GetMapping("/getCustomerByAdharcard")
    public ResponseEntity<CustomerDto> getCustomerByAdharcard(@RequestHeader Long adharcardNumber) {
        CustomerDto cust = customerService.getCustomerByAdharcard(adharcardNumber);
        return ResponseEntity.ok(cust);
    }

    @GetMapping("/updateCustomerByEmail")
    public ResponseEntity<CustomerDto> updateCustomerByEmail(@RequestHeader String email, @RequestBody CustomerDto customerDto) {
        return ResponseEntity.ok(customerService.updateCustomerByEmail(email, customerDto));
    }
    
    @PostMapping("/createAccount")
    public ResponseEntity<AccountDto> createAccount(@RequestBody AccountCreateDto account) {
        return ResponseEntity.ok(accountService.createAccount(account));
    }

    
    
}

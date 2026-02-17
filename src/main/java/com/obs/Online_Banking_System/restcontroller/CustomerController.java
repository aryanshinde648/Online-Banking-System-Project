package com.obs.Online_Banking_System.restcontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.obs.Online_Banking_System.dto.AccountCreateDto;
import com.obs.Online_Banking_System.dto.AccountResponseDto;
import com.obs.Online_Banking_System.dto.CustomerDto;
import com.obs.Online_Banking_System.service.AccountService;
import com.obs.Online_Banking_System.service.CustomerService;


@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/registerCustomer")
    public ResponseEntity<Map<String, Object>> registerCustomer(@RequestBody CustomerDto customerDto) {
        Map<String, Object> response = new HashMap<>();

        try {
            CustomerDto cust = customerService.registerCustomer(customerDto);
            response.put("success",true);
            response.put("message","Customer registered successfully");
            response.put("Customer",cust);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        
    }

    @PostMapping("/createAccount")
    public ResponseEntity<Map<String,Object>> createAccount(@RequestBody AccountCreateDto account) {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("success",true);
            response.put("message", "Account Created successfully");
            response.put("Account", accountService.createAccount(account));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/getmyAccount")
    public ResponseEntity<AccountResponseDto> getMethodName(@RequestHeader String param) {
        return ResponseEntity.ok(accountService.getmyAccount(param));
    }
    
    
}

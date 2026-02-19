package com.obs.Online_Banking_System.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.obs.Online_Banking_System.dto.AccountCreateDto;
import com.obs.Online_Banking_System.dto.CustomerDto;
import com.obs.Online_Banking_System.service.AccountService;
import com.obs.Online_Banking_System.service.CustomerService;


@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/register-customer")
    public String registerCustomer(Model model) {
        model.addAttribute("customer", new CustomerDto());
        return "register-customer";
        
    }

    @PostMapping("/createAccount")
    public ResponseEntity<Map<String,Object>> createAccount(@RequestBody AccountCreateDto account) {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("Account", accountService.createAccount(account));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/getmyAccount")
    public ResponseEntity<Map<String,Object>> getmyAccount(@RequestHeader String param) {
        Map<String,Object> response = new HashMap<>();
        try {
            response.put("Account", ResponseEntity.ok(accountService.getmyAccount(param)));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
}

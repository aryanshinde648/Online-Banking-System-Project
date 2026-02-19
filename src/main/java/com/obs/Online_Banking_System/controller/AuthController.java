package com.obs.Online_Banking_System.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.obs.Online_Banking_System.dto.CustomerDto;
import com.obs.Online_Banking_System.service.CustomerService;


import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/register-customer")
    public String registerCustomer(Model model, @ModelAttribute CustomerDto customerDto) {
        Map<String,Object> response = customerService.registerCustomerMap(customerDto);
        model.addAttribute("customer", customerDto);

        if (response.containsKey("email-error")) {
            model.addAttribute("email-error", "Email already exists");
        }

        if (response.containsKey("adhar-error")) {
            model.addAttribute("adhar-error", "Customer already exists");
        }

        model.addAttribute("message", "Customer Registration Successfull");

        return "register-customer";
    }
    
    
}

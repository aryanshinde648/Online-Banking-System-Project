package com.obs.Online_Banking_System.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.obs.Online_Banking_System.dto.CustomerDto;
import com.obs.Online_Banking_System.service.CustomerService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;



@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/register-customer")
    public String registerCustomer(Model model, @ModelAttribute("customer") CustomerDto customerDto) {
        Map<String,Object> response = customerService.registerCustomerMap(customerDto);
        
        if (response.containsKey("adhar-error") || response.containsKey("email-error")) {
            String msg = new String(response.get("adhar-error").toString());
            model.addAttribute("error",msg);
            return "register-customer";
        }

        model.addAttribute("customer", new CustomerDto());
        model.addAttribute("success", "Customer Registration Successfull");

        return "register-customer";
    }

    @PostMapping("/login-customer")
    public String loginCustomer(Model model, 
        @RequestParam(name = "email") String email, 
        @RequestParam(name = "password") String password, 
        HttpServletRequest request) throws IOException {

        Map<String,Object> response = new HashMap<>();

        response = customerService.athenticateCustomerMap(email,password);

        if (response.containsKey("error")) {
            String msg = new String(response.get("error").toString());
            model.addAttribute("error",msg);
            return "login-customer";
        }

        CustomerDto customerDto = (CustomerDto) response.get("customer");

        HttpSession session = request.getSession(true);
        session.setAttribute("loggedInCustomer", customerDto);
        session.setAttribute("customerId", customerDto.getCustomerId());
        session.setAttribute("email", customerDto.getEmail());
        session.setAttribute("adharcard", customerDto.getAdharcard());

        //wait for 10 seconds before redirecting to dashboard
        model.addAttribute("success", "Login successful");
        model.addAttribute("redirectDelayMs", 10);
        model.addAttribute("redirectUrl", "/dashboard");
        
        return "login-customer";
    }
    
    
    
}

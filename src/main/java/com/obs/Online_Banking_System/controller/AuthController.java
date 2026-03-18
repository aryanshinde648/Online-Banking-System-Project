package com.obs.Online_Banking_System.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;

import com.obs.Online_Banking_System.service.AccountService;
import com.obs.Online_Banking_System.service.CustomerService;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountService accountService;
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Invalidate session
            session.invalidate();

            response.put("success", true);
            response.put("message", "Logout successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error during logout: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/logout")
    public String logoutPage(HttpSession session) {
        try {
            // Invalidate session
            session.invalidate();
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
        }
        return "redirect:/login-customer";
    }
    
}

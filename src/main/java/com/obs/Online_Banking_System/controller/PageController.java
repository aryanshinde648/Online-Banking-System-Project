package com.obs.Online_Banking_System.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.obs.Online_Banking_System.dto.CustomerDto;

import jakarta.servlet.http.HttpSession;


@Controller
public class PageController {

    @GetMapping("/login-customer")
    public String loginCustomer(HttpSession session) {
        CustomerDto loggedInCustomer = (CustomerDto) session.getAttribute("loggedInUser");
        if (loggedInCustomer != null) {
            return "redirect:/dashboard";
        }
        return "login-customer";
    }

}

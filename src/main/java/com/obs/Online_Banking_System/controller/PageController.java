package com.obs.Online_Banking_System.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.obs.Online_Banking_System.dto.AdminDto;
import com.obs.Online_Banking_System.dto.CustomerDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;




@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/null")
    public String nullPageReturn() {
        return "home";
    }

    @GetMapping("/login-customer")
    public String loginCustomer(HttpSession session, HttpServletRequest request, Model model) {
        CustomerDto loggedInCustomer = (CustomerDto) session.getAttribute("loggedInCustomer");
        if (loggedInCustomer != null) {
            return "redirect:/customer/dashboard-customer";
        }
        model.addAttribute("requestURI", request.getRequestURI());
        return "login-customer";
    }

    @GetMapping("/login-admin")
    public String loginAdmin(HttpSession session, HttpServletRequest request, Model model) {
        AdminDto loggedInAdmin = (AdminDto) session.getAttribute("loggedInAdmin");
        if (loggedInAdmin != null) {
            return "redirect:/admin/dashboard-admin";
        }
        model.addAttribute("requestURI", request.getRequestURI());
        return "login-admin";
    }
    

}


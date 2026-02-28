package com.obs.Online_Banking_System.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


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
    public ResponseEntity<CustomerDto> updateCustomerByEmail(@RequestHeader String email,
            @RequestBody CustomerDto customerDto) {
        return ResponseEntity.ok(customerService.updateCustomerByEmail(email, customerDto));
    }

    @PostMapping("/createAccount")
    public ResponseEntity<AccountDto> createAccount(@RequestBody AccountCreateDto account) {
        return ResponseEntity.ok(accountService.createAccount(account));
    }

    // ---- Page routes ----

    /** Serve the admin registration page */
    @GetMapping("/register-admin")
    public String showRegisterAdminPage(Model model) {
        model.addAttribute("admin", new AdminDto());
        return "register-admin";
    }

    /** Handle admin registration form submission */
    @PostMapping("/register-admin")
    public String handleRegisterAdmin(
            @ModelAttribute("admin") AdminDto adminDto,
            Model model) {
        try {
            adminService.register(adminDto);
            model.addAttribute("success", "Admin account created successfully! You can now log in.");
            model.addAttribute("admin", new AdminDto());
        } catch (Exception e) {
            model.addAttribute("error",
                    e.getMessage() != null ? e.getMessage() : "Registration failed. Please try again.");
            model.addAttribute("admin", adminDto);
        }
        return "register-admin";
    }

    @GetMapping("/login")
    public String loginAdmin(Model model) {
        model.addAttribute("admin",new AdminDto());
        return new String();
    }
    
    @PostMapping("/login-admin")
    public String loginAdmin(Model model, 
        @RequestParam(name = "email") String email, 
        @RequestParam(name = "password") String password,
        HttpServletRequest request) throws IOException {

        Map<String,Object> response = new HashMap<>();

        response = adminService.athenticateAdminMap(email,password);

        if (response.containsKey("error")) {
            String msg = new String(response.get("error").toString());
            model.addAttribute("error",msg);
            return "login-admin";
        }

        AdminDto adminDto = (AdminDto) response.get("admin");

        HttpSession session = request.getSession(true);
        session.setAttribute("loggedInAdmin", adminDto);
        session.setAttribute("adminId", adminDto.getAdminId());
        session.setAttribute("email", adminDto.getEmail());
        session.setAttribute("adharcard", adminDto.getAdharcard());

        //wait for 10 seconds before redirecting to dashboard
        model.addAttribute("success", "Login successful");
        model.addAttribute("redirectDelayMs", 10);
        model.addAttribute("redirectUrl", "/admin/dashboard-admin");
        
        return "login-admin";
    }

    @GetMapping("/dashboard-admin")
    public String dashboard(HttpSession session, RedirectAttributes redirectAttributes) {
         // Check if admin is logged in by looking for the session attribute
        AdminDto adminDto = (AdminDto) session.getAttribute("loggedInAdmin");

        if (adminDto != null) {
            // admin is authenticated, allow access to dashboard
            return "dashboard-admin";
        }else{
            // admin is not authenticated, redirect to login
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to access the dashboard");
            return "redirect:/login-admin";
        }
    }
    
}

package com.obs.Online_Banking_System.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.ResponseBody;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.obs.Online_Banking_System.dto.AccountCreateDto;
import com.obs.Online_Banking_System.dto.CustomerDto;
import com.obs.Online_Banking_System.service.AccountService;
import com.obs.Online_Banking_System.service.CustomerService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody AccountCreateDto account,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        CustomerDto cust = (CustomerDto) session.getAttribute("loggedInCustomer");
        String email = cust.getEmail();
        account.setEmail(email);

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
    public ResponseEntity<Map<String, Object>> getmyAccount(@RequestHeader String param) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("Account", ResponseEntity.ok(accountService.getmyAccount(param)));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/dashboard-customer")
    public String dashboard(HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if customer is logged in by looking for the session attribute
        CustomerDto customerDto = (CustomerDto) session.getAttribute("loggedInCustomer");

        if (customerDto != null) {
            // customer is authenticated, allow access to dashboard
            return "dashboard-customer";
        } else {
            // customer is not authenticated, redirect to login
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to access the dashboard");
            return "redirect:/login-customer";
        }
    }

    @PostMapping("/register-customer")
    public String registerCustomer(Model model, @ModelAttribute("customer") CustomerDto customerDto) {
        Map<String, Object> response = customerService.registerCustomerMap(customerDto);

        if (response.containsKey("adhar-error") || response.containsKey("email-error")) {
            String msg = new String(response.get("adhar-error").toString());
            model.addAttribute("error", msg);
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

        Map<String, Object> response = new HashMap<>();

        response = customerService.athenticateCustomerMap(email, password);

        if (response.containsKey("error")) {
            String msg = new String(response.get("error").toString());
            model.addAttribute("error", msg);
            return "login-customer";
        }

        CustomerDto customerDto = (CustomerDto) response.get("customer");

        HttpSession session = request.getSession(true);
        session.setAttribute("loggedInCustomer", customerDto);
        session.setAttribute("customerId", customerDto.getCustomerId());
        session.setAttribute("email", customerDto.getEmail());
        session.setAttribute("adharcard", customerDto.getAdharcard());

        // wait for 10 seconds before redirecting to dashboard
        model.addAttribute("success", "Login successful");
        model.addAttribute("redirectDelayMs", 10);
        model.addAttribute("redirectUrl", "/customer/dashboard-customer");

        return "login-customer";
    }

    @PutMapping({ "/profile-customer/{id}", "/profile/{id}" })
    @ResponseBody
    public CustomerDto updateProfile(@PathVariable("id") Long id, @RequestBody CustomerDto customerDto,
            HttpSession session) {
        Long custid = id;
        if (id == null) {
            CustomerDto cust = (CustomerDto) session.getAttribute("loggedInCustomer");
            custid = cust.getCustomerId();
        }
        CustomerDto updatedCustomer = customerService.updateCustomerById(custid, customerDto);
        session.setAttribute("loggedInCustomer", updatedCustomer);
        session.setAttribute("customerId", customerDto.getCustomerId());
        session.setAttribute("email", customerDto.getEmail());
        session.setAttribute("adharcard", customerDto.getAdharcard());

        return updatedCustomer;
    }

    @GetMapping({ "/profile", "/profile-customer" })
    public String showProfile(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        CustomerDto customerDto = (CustomerDto) session.getAttribute("loggedInCustomer");

        if (customerDto != null) {
            // Expose logged-in customer and id to the view (templates should use these
            // model attributes)
            Object logged = session.getAttribute("loggedInCustomer");
            model.addAttribute("loggedInCustomer", logged);
            model.addAttribute("customerId", session.getAttribute("customerId"));

            model.addAttribute("customer", new CustomerDto());
            return "profile-customer";
        } else {
            // customer is not authenticated, redirect to login
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to access the dashboard");
            return "redirect:/login-customer";
        }
    }

    /**
     * POST /customer/api/change-password
     * Body: { "oldPassword": "...", "newPassword": "..." }
     */
    @PostMapping("/api/change-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody Map<String, String> body,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        CustomerDto cust = (CustomerDto) session.getAttribute("loggedInCustomer");
        if (cust == null) {
            result.put("error", "Not logged in");
            return ResponseEntity.status(401).body(result);
        }
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            result.put("error", "New password cannot be empty");
            return ResponseEntity.badRequest().body(result);
        }
        String msg = customerService.changePassword(cust.getCustomerId(), oldPassword, newPassword);
        if (msg.contains("incorrect")) {
            result.put("error", msg);
            return ResponseEntity.badRequest().body(result);
        }
        result.put("success", true);
        result.put("message", msg);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /customer/api/change-pin
     * Body: { "oldPin": "...", "newPin": "..." }
     */
    @PostMapping("/api/change-pin")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changePin(
            @RequestBody Map<String, String> body,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        CustomerDto cust = (CustomerDto) session.getAttribute("loggedInCustomer");
        if (cust == null) {
            result.put("error", "Not logged in");
            return ResponseEntity.status(401).body(result);
        }
        String oldPin = body.get("oldPin");
        String newPin = body.get("newPin");
        if (newPin == null || newPin.length() != 6) {
            result.put("error", "New PIN must be exactly 6 digits");
            return ResponseEntity.badRequest().body(result);
        }
        String msg = customerService.changePin(cust.getCustomerId(), oldPin, newPin);
        if (msg.contains("incorrect")) {
            result.put("error", msg);
            return ResponseEntity.badRequest().body(result);
        }
        result.put("success", true);
        result.put("message", msg);
        return ResponseEntity.ok(result);
    }

}

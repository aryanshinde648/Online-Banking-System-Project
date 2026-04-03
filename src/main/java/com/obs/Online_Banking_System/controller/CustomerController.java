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
import com.obs.Online_Banking_System.enumDto.AuthStatus;
import com.obs.Online_Banking_System.enumDto.OtpType;
import com.obs.Online_Banking_System.enumDto.OtpVerificationResult;
import com.obs.Online_Banking_System.service.AccountService;
import com.obs.Online_Banking_System.service.CustomerService;
import com.obs.Online_Banking_System.service.OtpService;
import com.obs.Online_Banking_System.service.TwoFactorService;
import com.obs.Online_Banking_System.service.impl.CustomerServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private TwoFactorService twoFactorService;

    /** Max failed TOTP login attempts before temp session is cleared */
    private static final int MAX_TOTP_LOGIN_ATTEMPTS = 5;

    // ── Account ───────────────────────────────────────────────────────────────

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

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard-customer")
    public String dashboard(HttpSession session, RedirectAttributes redirectAttributes) {
        CustomerDto customerDto = (CustomerDto) session.getAttribute("loggedInCustomer");
        if (customerDto != null) {
            return "dashboard-customer";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to access the dashboard");
            return "redirect:/login-customer";
        }
    }

    // ── Registration ──────────────────────────────────────────────────────────

    @PostMapping("/register-customer")
    public String registerCustomer(Model model, @ModelAttribute("customer") CustomerDto customerDto, HttpSession session) {
        Map<String, Object> response = customerService.registerCustomerMap(customerDto);

        if (response.containsKey("adhar-error")) {
            model.addAttribute("error", response.get("adhar-error").toString());
            model.addAttribute("customer", new CustomerDto());
            return "register-customer";
        }
        if (response.containsKey("email-error")) {
            model.addAttribute("error", response.get("email-error").toString());
            model.addAttribute("customer", new CustomerDto());
            return "register-customer";
        }
        if (response.containsKey("dob-error")) {
            model.addAttribute("error", response.get("dob-error").toString());
            model.addAttribute("customer", new CustomerDto());
            return "register-customer";
        }

        // Add to session to save after email verification
        session.setAttribute("pendingRegistration", customerDto);

        // Redirect to email verification page
        String email = (String) response.get("email");
        if (email == null && response.get("customer") instanceof CustomerDto cd) {
            email = cd.getEmail();
        }

        String maskedEmail = CustomerServiceImpl.maskEmail(email);
        model.addAttribute("email", email);
        model.addAttribute("maskedEmail", maskedEmail);

        if (response.containsKey("otp-error")) {
            model.addAttribute("warning", response.get("otp-error").toString());
        }

        return "verify-email";
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @PostMapping("/login-customer")
    public String loginCustomer(Model model,
            @RequestParam(name = "email") String email,
            @RequestParam(name = "password") String password,
            HttpServletRequest request) throws IOException {

        Map<String, Object> response = customerService.athenticateCustomerMap(email, password);
        AuthStatus status = (AuthStatus) response.get("status");

        if (status == AuthStatus.INVALID_CREDENTIALS) {
            model.addAttribute("error", response.get("error"));
            return "login-customer";
        }

        if (status == AuthStatus.EMAIL_NOT_VERIFIED) {
            // Resend OTP silently if needed, then send to verify page
            String maskedEmail = CustomerServiceImpl.maskEmail(email);
            model.addAttribute("email", email);
            model.addAttribute("maskedEmail", maskedEmail);
            model.addAttribute("info", "Your email is not verified. An OTP has been sent to " + maskedEmail);
            try {
                otpService.generateAndSendOtp(email, OtpType.EMAIL_VERIFICATION);
            } catch (Exception ignored) { /* already handled in service */ }
            return "verify-email";
        }

        if (status == AuthStatus.AUTHENTICATOR_OTP_REQUIRED) {
            // Store customerId temporarily in session for TOTP login verification
            Long customerId = (Long) response.get("customerId");
            HttpSession session = request.getSession(true);
            session.setAttribute("tempCustomerId", customerId);
            session.setAttribute("totpAttempts", 0);
            return "redirect:/customer/authenticator-login";
        }

        if (status == AuthStatus.OTP_REQUIRED) {
            String maskedEmail = CustomerServiceImpl.maskEmail(email);
            model.addAttribute("email", email);
            model.addAttribute("maskedEmail", maskedEmail);
            return "login-otp";
        }

        // Fallback error
        model.addAttribute("error", response.containsKey("error") ? response.get("error") : "Login failed.");
        return "login-customer";
    }

    // ── Email OTP Verification ────────────────────────────────────────────────

    @GetMapping("/verify-email")
    public String showVerifyEmail(@RequestParam(required = false) String email, Model model) {
        if (email != null) {
            model.addAttribute("email", email);
            model.addAttribute("maskedEmail", CustomerServiceImpl.maskEmail(email));
        }
        return "verify-email";
    }

    @PostMapping("/verify-email")
    public String verifyEmail(
            @RequestParam String email,
            @RequestParam String otp,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        OtpVerificationResult result = customerService.verifyEmailOtp(email, otp);
        String maskedEmail = CustomerServiceImpl.maskEmail(email);

        switch (result) {
            case SUCCESS -> {
                CustomerDto pendingDto = (CustomerDto) session.getAttribute("pendingRegistration");
                if (pendingDto != null && pendingDto.getEmail().equals(email)) {
                    customerService.registerVerifiedCustomer(pendingDto);
                    session.removeAttribute("pendingRegistration");
                }

                redirectAttributes.addFlashAttribute("success",
                        "✅ Email verified successfully! Please log in.");
                return "redirect:/login-customer";
            }
            case EXPIRED -> {
                model.addAttribute("email", email);
                model.addAttribute("maskedEmail", maskedEmail);
                model.addAttribute("error", "OTP has expired. Please request a new one.");
                return "verify-email";
            }
            case MAX_ATTEMPTS_REACHED -> {
                model.addAttribute("email", email);
                model.addAttribute("maskedEmail", maskedEmail);
                model.addAttribute("error",
                        "Too many wrong attempts. Please request a new OTP.");
                return "verify-email";
            }
            default -> {
                model.addAttribute("email", email);
                model.addAttribute("maskedEmail", maskedEmail);
                model.addAttribute("error", "Invalid OTP. Please try again.");
                return "verify-email";
            }
        }
    }

    // ── Login OTP (2FA) ───────────────────────────────────────────────────────

    @GetMapping("/login-otp")
    public String showLoginOtp(@RequestParam(required = false) String email, Model model) {
        if (email != null) {
            model.addAttribute("email", email);
            model.addAttribute("maskedEmail", CustomerServiceImpl.maskEmail(email));
        }
        return "login-otp";
    }

    @PostMapping("/login-otp")
    public String verifyLoginOtp(
            @RequestParam String email,
            @RequestParam String otp,
            Model model,
            HttpServletRequest request) {

        Map<String, Object> result = customerService.verifyLoginOtp(email, otp);
        OtpVerificationResult otpResult = (OtpVerificationResult) result.get("result");
        String maskedEmail = CustomerServiceImpl.maskEmail(email);

        switch (otpResult) {
            case SUCCESS -> {
                // ✅ Session created ONLY here — after full OTP verification
                CustomerDto customerDto = (CustomerDto) result.get("customer");
                HttpSession session = request.getSession(true);
                session.setAttribute("loggedInCustomer", customerDto);
                session.setAttribute("customerId", customerDto.getCustomerId());
                session.setAttribute("email", customerDto.getEmail());
                session.setAttribute("adharcard", customerDto.getAdharcard());
                return "redirect:/customer/dashboard-customer";
            }
            case EXPIRED -> {
                model.addAttribute("email", email);
                model.addAttribute("maskedEmail", maskedEmail);
                model.addAttribute("error", "OTP has expired. Please log in again.");
                return "login-otp";
            }
            case MAX_ATTEMPTS_REACHED -> {
                model.addAttribute("email", email);
                model.addAttribute("maskedEmail", maskedEmail);
                model.addAttribute("error",
                        "Too many wrong attempts. Please log in again to receive a new OTP.");
                return "login-otp";
            }
            default -> {
                model.addAttribute("email", email);
                model.addAttribute("maskedEmail", maskedEmail);
                model.addAttribute("error", "Invalid OTP. Please try again.");
                return "login-otp";
            }
        }
    }

    // ── Resend OTP (AJAX) ─────────────────────────────────────────────────────

    @PostMapping("/api/resend-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resendOtp(
            @RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        String email = body.get("email");
        String typeStr = body.get("type"); // "EMAIL_VERIFICATION" or "LOGIN_2FA"

        if (email == null || email.isBlank()) {
            response.put("error", "Email is required.");
            return ResponseEntity.badRequest().body(response);
        }

        OtpType otpType;
        try {
            otpType = OtpType.valueOf(typeStr);
        } catch (Exception e) {
            response.put("error", "Invalid OTP type.");
            return ResponseEntity.badRequest().body(response);
        }

        long remaining = otpService.getResendCooldownRemaining(email, otpType);
        if (remaining > 0) {
            response.put("error", "Please wait " + remaining + " second(s) before resending.");
            response.put("cooldownRemaining", remaining);
            return ResponseEntity.status(429).body(response);
        }

        try {
            otpService.resendOtp(email, otpType);
            response.put("success", true);
            response.put("message", "OTP sent to " + CustomerServiceImpl.maskEmail(email));
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(429).body(response);
        }

        return ResponseEntity.ok(response);
    }

    // ── Profile ───────────────────────────────────────────────────────────────

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
            Object logged = session.getAttribute("loggedInCustomer");
            model.addAttribute("loggedInCustomer", logged);
            model.addAttribute("customerId", session.getAttribute("customerId"));
            model.addAttribute("customer", new CustomerDto());
            return "profile-customer";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to access the dashboard");
            return "redirect:/login-customer";
        }
    }

    // ── Password & PIN ────────────────────────────────────────────────────────

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

    // ── Authenticator 2FA Setup ─────────────────────────────────────────────

    /** GET /customer/authenticator-setup — Show the TOTP setup page */
    @GetMapping("/authenticator-setup")
    public String showAuthenticatorSetup(HttpSession session, RedirectAttributes redirectAttributes) {
        CustomerDto cust = (CustomerDto) session.getAttribute("loggedInCustomer");
        if (cust == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login first.");
            return "redirect:/login-customer";
        }
        return "authenticator-setup";
    }

    /**
     * POST /customer/enable-authenticator
     * Generates a TOTP secret, saves it, and returns the QR URL + PNG image.
     * Returns { qrUrl, message } — secretKey is NEVER exposed.
     */
    @PostMapping("/enable-authenticator")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> enableAuthenticator(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        CustomerDto cust = (CustomerDto) session.getAttribute("loggedInCustomer");
        if (cust == null) {
            result.put("error", "Not logged in.");
            return ResponseEntity.status(401).body(result);
        }
        try {
            Map<String, Object> svcResult = customerService.enableAuthenticator(cust.getCustomerId());
            return ResponseEntity.ok(svcResult);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * GET /customer/api/qr-code — Returns the QR code image as PNG (base64 is handled client-side).
     * The secret is fetched from DB; never from session or DTO.
     */
    @GetMapping(value = "/api/qr-code", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQRCodeImage(
            @org.springframework.web.bind.annotation.RequestParam String qrUrl) {
        try {
            byte[] image = twoFactorService.generateQRCodeImage(qrUrl, 250, 250);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(image);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /customer/verify-authenticator
     * Verifies the OTP from the setup flow and enables 2FA on the account.
     */
    @PostMapping("/verify-authenticator")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyAuthenticator(
            @RequestBody Map<String, String> body,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        CustomerDto cust = (CustomerDto) session.getAttribute("loggedInCustomer");
        if (cust == null) {
            result.put("error", "Not logged in.");
            return ResponseEntity.status(401).body(result);
        }
        String otpStr = body.get("otp");
        if (otpStr == null || otpStr.isBlank()) {
            result.put("error", "OTP is required.");
            return ResponseEntity.badRequest().body(result);
        }
        int otp;
        try {
            otp = Integer.parseInt(otpStr.trim());
        } catch (NumberFormatException e) {
            result.put("error", "OTP must be a 6-digit number.");
            return ResponseEntity.badRequest().body(result);
        }
        Map<String, Object> svcResult = customerService.verifyAuthenticatorSetup(cust.getCustomerId(), otp);
        if (svcResult.containsKey("success")) {
            // Update session to reflect is2faEnabled = true
            CustomerDto updatedCust = customerService.getCustomerById(cust.getCustomerId());
            session.setAttribute("loggedInCustomer", updatedCust);
            return ResponseEntity.ok(svcResult);
        }
        return ResponseEntity.badRequest().body(svcResult);
    }

    // ── Authenticator 2FA Login ──────────────────────────────────────────────

    /** GET /customer/authenticator-login — Show TOTP verification page during login */
    @GetMapping("/authenticator-login")
    public String showAuthenticatorLogin(HttpSession session, RedirectAttributes redirectAttributes) {
        Long tempId = (Long) session.getAttribute("tempCustomerId");
        if (tempId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Session expired. Please log in again.");
            return "redirect:/login-customer";
        }
        return "authenticator-login";
    }

    /**
     * POST /customer/verify-authenticator-login
     * Verifies TOTP during login. On success: creates full session.
     * On failure: tracks attempts; clears tempCustomerId after MAX_TOTP_LOGIN_ATTEMPTS.
     */
    @PostMapping("/verify-authenticator-login")
    public String verifyAuthenticatorLogin(
            @RequestParam String otp,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long customerId = (Long) session.getAttribute("tempCustomerId");
        if (customerId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Session expired. Please log in again.");
            return "redirect:/login-customer";
        }

        int otpInt;
        try {
            otpInt = Integer.parseInt(otp.trim());
        } catch (NumberFormatException e) {
            model.addAttribute("error", "OTP must be a 6-digit number.");
            return "authenticator-login";
        }

        Map<String, Object> result = customerService.verifyAuthenticatorLogin(customerId, otpInt);

        if (result.containsKey("success")) {
            // Build full session and clear temp state
            CustomerDto customerDto = (CustomerDto) result.get("customer");
            session.setAttribute("loggedInCustomer", customerDto);
            session.setAttribute("customerId", customerDto.getCustomerId());
            session.setAttribute("email", customerDto.getEmail());
            session.setAttribute("adharcard", customerDto.getAdharcard());
            session.removeAttribute("tempCustomerId");
            session.removeAttribute("totpAttempts");
            return "redirect:/customer/dashboard-customer";
        }

        // Track failed attempts
        Integer attempts = (Integer) session.getAttribute("totpAttempts");
        if (attempts == null) attempts = 0;
        attempts++;
        session.setAttribute("totpAttempts", attempts);

        if (attempts >= MAX_TOTP_LOGIN_ATTEMPTS) {
            session.removeAttribute("tempCustomerId");
            session.removeAttribute("totpAttempts");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Too many failed attempts. Please log in again.");
            return "redirect:/login-customer";
        }

        model.addAttribute("error", result.get("error"));
        model.addAttribute("attemptsLeft", MAX_TOTP_LOGIN_ATTEMPTS - attempts);
        return "authenticator-login";
    }
}

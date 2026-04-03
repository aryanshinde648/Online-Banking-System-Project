package com.obs.Online_Banking_System.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.obs.Online_Banking_System.dto.CustomerDto;
import com.obs.Online_Banking_System.entity.Customer;
import com.obs.Online_Banking_System.enumDto.AuthStatus;
import com.obs.Online_Banking_System.enumDto.OtpType;
import com.obs.Online_Banking_System.enumDto.OtpVerificationResult;
import com.obs.Online_Banking_System.mapper.CustomerConversion;
import com.obs.Online_Banking_System.repository.CustomerRepository;
import com.obs.Online_Banking_System.service.CustomerService;
import com.obs.Online_Banking_System.service.OtpService;
import com.obs.Online_Banking_System.service.TwoFactorService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerConversion customerConversion;

    @Autowired
    OtpService otpService;

    @Autowired
    TwoFactorService twoFactorService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> registerCustomerMap(CustomerDto customerDto) {
        Map<String, Object> response = new HashMap<>();

        Customer newCust = customerConversion.toCustomerEntity(customerDto);

        if (customerDto.getDob() != null && !customerDto.getDob().isBlank()) {
            try {
                java.time.LocalDate dob = java.time.LocalDate.parse(customerDto.getDob());
                int age = java.time.Period.between(dob, java.time.LocalDate.now()).getYears();
                if (age <= 18) {
                    response.put("dob-error", "Age must be greater than 18 to register");
                    log.warn("Customer registration failed: Age is not greater than 18");
                    return response;
                }
            } catch (java.time.format.DateTimeParseException e) {
                response.put("dob-error", "Invalid Date of Birth format");
                return response;
            }
        }

        if (customerRepository.findByAdharcard(customerDto.getAdharcard()).isPresent()) {
            response.put("adhar-error", "Customer with Adharcard No. " + newCust.getAdharcard() + " already exists");
            log.warn("Customer with Adharcard No. {} already exists", newCust.getAdharcard());
            return response;
        }

        if (customerRepository.findByEmail(customerDto.getEmail()).isPresent()) {
            response.put("email-error", "Customer with Email " + newCust.getEmail() + " already exists");
            log.warn("Customer with Email {} already exists", newCust.getEmail());
            return response;
        }

        // Hash the password before saving (optional here, but done just in case we return customer)
        if (newCust.getPassword() != null && !newCust.getPassword().isBlank()) {
            newCust.setPassword(passwordEncoder.encode(newCust.getPassword()));
        }
        
        log.info("New customer registration initiated: {} — awaiting email verification", newCust.getEmail());

        // Generate and send EMAIL_VERIFICATION OTP
        try {
            otpService.generateAndSendOtp(newCust.getEmail(), OtpType.EMAIL_VERIFICATION);
        } catch (Exception e) {
            log.error("Failed to send verification OTP to {}: {}", newCust.getEmail(), e.getMessage());
            response.put("otp-error", "Registration successful but email sending failed. Contact support.");
        }

        response.put("customer", customerConversion.toCustomerDto(newCust));
        response.put("email", newCust.getEmail());
        response.put("status", "EMAIL_VERIFICATION_SENT");

        return response;
    }

    @Override
    public CustomerDto registerCustomer(CustomerDto customerDto) {

        Customer newCust = customerConversion.toCustomerEntity(customerDto);

        if (customerDto.getDob() != null && !customerDto.getDob().isBlank()) {
            try {
                java.time.LocalDate dob = java.time.LocalDate.parse(customerDto.getDob());
                int age = java.time.Period.between(dob, java.time.LocalDate.now()).getYears();
                if (age <= 18) {
                    throw new RuntimeException("Age must be greater than 18 to register");
                }
            } catch (java.time.format.DateTimeParseException e) {
                throw new RuntimeException("Invalid Date of Birth format");
            }
        }

        if (customerRepository.findByAdharcard(customerDto.getAdharcard()).isPresent()) {
            throw new RuntimeException("Customer with Adharcard No. " + newCust.getAdharcard() + " already exists");
        }

        // Hash the password before saving
        if (newCust.getPassword() != null && !newCust.getPassword().isBlank()) {
            newCust.setPassword(passwordEncoder.encode(newCust.getPassword()));
        }
        customerRepository.save(newCust);

        return customerConversion.toCustomerDto(newCust);
    }

    @Override
    public CustomerDto registerVerifiedCustomer(CustomerDto customerDto) {

        Customer newCust = customerConversion.toCustomerEntity(customerDto);

        if (customerDto.getDob() != null && !customerDto.getDob().isBlank()) {
            try {
                java.time.LocalDate dob = java.time.LocalDate.parse(customerDto.getDob());
                int age = java.time.Period.between(dob, java.time.LocalDate.now()).getYears();
                if (age <= 18) {
                    throw new RuntimeException("Age must be greater than 18 to register");
                }
            } catch (java.time.format.DateTimeParseException e) {
                throw new RuntimeException("Invalid Date of Birth format");
            }
        }

        if (customerRepository.findByAdharcard(customerDto.getAdharcard()).isPresent()) {
            throw new RuntimeException("Customer with Adharcard No. " + newCust.getAdharcard() + " already exists");
        }

        // Hash the password before saving
        if (newCust.getPassword() != null && !newCust.getPassword().isBlank()) {
            newCust.setPassword(passwordEncoder.encode(newCust.getPassword()));
        }
        newCust.setEmailVerified(true);
        customerRepository.save(newCust);

        return customerConversion.toCustomerDto(newCust);
    }

    @Override
    public CustomerDto getCustomerById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return customerConversion.toCustomerDto(customer);
    }

    @Override
    public CustomerDto getCustomerByEmail(String email) {

        if (customerRepository.findByEmail(email).isEmpty()) {
            throw new RuntimeException("Customer not found with email: " + email);
        }

        Customer customer = customerRepository.getByEmail(email);

        return customerConversion.toCustomerDto(customer);
    }

    @Override
    public CustomerDto updateCustomerById(Long id, CustomerDto customerDto) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (customerDto.getFname() != null)
            existingCustomer.setFname(customerDto.getFname());
        if (customerDto.getLname() != null)
            existingCustomer.setLname(customerDto.getLname());
        if (customerDto.getEmail() != null)
            existingCustomer.setEmail(customerDto.getEmail());
        if (customerDto.getPhone() != null)
            existingCustomer.setPhone(customerDto.getPhone());
        if (customerDto.getAddress() != null)
            existingCustomer.setAddress(customerDto.getAddress());
        if (customerDto.getPassword() != null && !customerDto.getPassword().isBlank()) {
            existingCustomer.setPassword(passwordEncoder.encode(customerDto.getPassword()));
        }

        customerRepository.save(existingCustomer);

        return customerConversion.toCustomerDto(existingCustomer);
    }

    @Override
    public CustomerDto updateCustomerByEmail(String email, CustomerDto customerDto) {

        if (customerRepository.findByEmail(email).isEmpty()) {
            throw new RuntimeException("Customer not found with email: " + email);
        }

        Customer existingCustomer = customerRepository.getByEmail(email);

        existingCustomer.setFname(customerDto.getFname());
        existingCustomer.setLname(customerDto.getLname());
        existingCustomer.setEmail(customerDto.getEmail());
        if (customerDto.getPassword() != null && !customerDto.getPassword().isBlank()) {
            existingCustomer.setPassword(passwordEncoder.encode(customerDto.getPassword()));
        }
        existingCustomer.setAddress(customerDto.getAddress());
        existingCustomer.setPhone(customerDto.getPhone());

        customerRepository.save(existingCustomer);

        return customerConversion.toCustomerDto(existingCustomer);
    }

    @Override
    public String deleteCustomer(Long id) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customerRepository.delete(existingCustomer);

        return "Customer deleted successfully";
    }

    @Override
    public String changePassword(Long id, String oldPassword, String newPassword) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!passwordEncoder.matches(oldPassword, existingCustomer.getPassword())) {
            return "Old password is incorrect";
        }

        existingCustomer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(existingCustomer);

        return "Password changed successfully";
    }

    @Override
    public CustomerDto getCustomerByAdharcard(Long adharcard) {

        if (customerRepository.findByAdharcard(adharcard).isEmpty()) {
            throw new RuntimeException("Customer not found with adharcard: " + adharcard);
        }

        Customer customer = customerRepository.getByAdharcard(adharcard);

        return customerConversion.toCustomerDto(customer);
    }

    @Override
    public CustomerDto athenticateCustomer(String email, String pass) {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer Not Found with email id"));

        if (!passwordEncoder.matches(pass, customer.getPassword())) {
            throw new RuntimeException("Invalid Email or Password");
        }

        log.info("Customer logged in with email: {}" + email);
        return customerConversion.toCustomerDto(customer);
    }

    @Override
    public Map<String, Object> athenticateCustomerMap(String email, String pass) {
        Map<String, Object> response = new HashMap<>();

        Customer customer = customerRepository.getByEmail(email);

        if (customer == null) {
            response.put("error", "Invalid Email or Password");
            response.put("status", AuthStatus.INVALID_CREDENTIALS);
            return response;
        }

        if (!passwordEncoder.matches(pass, customer.getPassword())) {
            response.put("error", "Invalid Email or Password");
            response.put("status", AuthStatus.INVALID_CREDENTIALS);
            return response;
        }

        // Check email verification
        if (!customer.isEmailVerified()) {
            log.warn("Login attempt by unverified email: {}", email);
            response.put("error", "Please verify your email before logging in.");
            response.put("status", AuthStatus.EMAIL_NOT_VERIFIED);
            response.put("email", email);
            return response;
        }

        // Password valid + email verified — choose 2FA method
        if (customer.is2faEnabled()) {
            // Customer uses Google Authenticator — do NOT send Email OTP
            log.info("Authenticator 2FA required for login: {}", email);
            response.put("status", AuthStatus.AUTHENTICATOR_OTP_REQUIRED);
            response.put("customerId", customer.getId());
            return response;
        }

        // Existing Email OTP flow (unchanged)
        try {
            otpService.generateAndSendOtp(email, OtpType.LOGIN_2FA);
        } catch (Exception e) {
            log.error("Failed to send 2FA OTP to {}: {}", email, e.getMessage());
            response.put("error", "Failed to send login OTP. Please try again.");
            return response;
        }

        log.info("2FA OTP sent for login: {}", email);
        response.put("status", AuthStatus.OTP_REQUIRED);
        response.put("email", email);
        return response;
    }

    @Override
    public List<CustomerDto> getAllCustomers() {
        List<Customer> cust = customerRepository.findAll();
        List<CustomerDto> custList = customerConversion.toCustomerDtoList(cust);
        return custList;
    }

    @Override
    public Long getAllCustomerCount() {
        return customerRepository.count();
    }

    @Override
    public String changePin(Long id, String oldPin, String newPin) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (oldPin != null && !oldPin.isBlank()) {
            if (customer.getPin() == null || !customer.getPin().equals(oldPin)) {
                return "Old PIN is incorrect";
            }
        }

        customer.setPin(newPin);
        customerRepository.save(customer);
        return "PIN changed successfully";
    }

    // ── OTP Verification Methods ─────────────────────────────────────────────

    @Override
    public OtpVerificationResult verifyEmailOtp(String email, String otp) {
        OtpVerificationResult result = otpService.verifyOtp(email, otp, OtpType.EMAIL_VERIFICATION);

        if (result == OtpVerificationResult.SUCCESS) {
            Customer customer = customerRepository.getByEmail(email);
            if (customer != null) {
                customer.setEmailVerified(true);
                customerRepository.save(customer);
                log.info("Email verified for customer: {}", email);
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> verifyLoginOtp(String email, String otp) {
        Map<String, Object> response = new HashMap<>();
        OtpVerificationResult result = otpService.verifyOtp(email, otp, OtpType.LOGIN_2FA);

        response.put("result", result);
        if (result == OtpVerificationResult.SUCCESS) {
            Customer customer = customerRepository.getByEmail(email);
            if (customer != null) {
                response.put("customer", customerConversion.toCustomerDto(customer));
                log.info("Login 2FA OTP verified for: {}", email);
            }
        }
        return response;
    }

    // ── Authenticator 2FA Methods ─────────────────────────────────────────────

    @Override
    public Map<String, Object> enableAuthenticator(Long customerId) {
        Map<String, Object> response = new HashMap<>();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        String secret = twoFactorService.generateSecret();
        customer.setSecretKey(secret);
        // is2faEnabled stays false until the user verifies the OTP
        customerRepository.save(customer);

        String qrUrl = twoFactorService.getQRBarcodeURL(customer.getEmail(), secret);
        log.info("TOTP secret generated for customer id={}", customerId);

        // secretKey is NOT returned — only the QR URL
        response.put("qrUrl", qrUrl);
        response.put("message", "Scan the QR code using your Authenticator app, then verify below.");
        return response;
    }

    @Override
    public Map<String, Object> verifyAuthenticatorSetup(Long customerId, int otp) {
        Map<String, Object> response = new HashMap<>();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (customer.getSecretKey() == null) {
            response.put("error", "No authenticator secret found. Please generate a QR code first.");
            return response;
        }

        if (!twoFactorService.verifyOTP(customer.getSecretKey(), otp)) {
            log.warn("TOTP setup verification failed for customer id={}", customerId);
            response.put("error", "Invalid OTP. Please try again.");
            return response;
        }

        customer.set2faEnabled(true);
        customerRepository.save(customer);
        log.info("Authenticator 2FA enabled for customer id={}", customerId);

        response.put("success", true);
        response.put("message", "Google Authenticator has been enabled for your account!");
        return response;
    }

    @Override
    public Map<String, Object> verifyAuthenticatorLogin(Long customerId, int otp) {
        Map<String, Object> response = new HashMap<>();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (customer.getSecretKey() == null || !customer.is2faEnabled()) {
            response.put("error", "Authenticator 2FA is not set up for this account.");
            return response;
        }

        if (!twoFactorService.verifyOTP(customer.getSecretKey(), otp)) {
            log.warn("TOTP login verification failed for customer id={}", customerId);
            response.put("error", "Invalid OTP. Please try again.");
            return response;
        }

        log.info("Authenticator 2FA login verified for customer id={}", customerId);
        response.put("success", true);
        response.put("customer", customerConversion.toCustomerDto(customer));
        return response;
    }

    // ── Email Masking Utility ────────────────────────────────────────────────

    /** Returns masked email: abc@gmail.com → a**@gmail.com */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 1) return email;
        return local.charAt(0) + "***@" + domain;
    }

}

package com.obs.Online_Banking_System.service;

import java.util.List;
import java.util.Map;

import com.obs.Online_Banking_System.dto.CustomerDto;

public interface CustomerService {

    public Map<String, Object> registerCustomerMap(CustomerDto customerDto);

    public CustomerDto registerCustomer(CustomerDto customerDto);

    public CustomerDto registerVerifiedCustomer(CustomerDto customerDto);

    public CustomerDto getCustomerById(Long id);

    public CustomerDto getCustomerByEmail(String email);

    public CustomerDto updateCustomerByEmail(String email, CustomerDto customerDto);

    public CustomerDto updateCustomerById(Long id, CustomerDto customerDto);

    public String deleteCustomer(Long id);

    public String changePassword(Long id, String oldPassword, String newPassword);

    public CustomerDto getCustomerByAdharcard(Long adharcard);

    CustomerDto athenticateCustomer(String email, String pass);

    Map<String, Object> athenticateCustomerMap(String email, String pass);

    List<CustomerDto> getAllCustomers();

    Long getAllCustomerCount();

    String changePin(Long id, String oldPin, String newPin);

    /**
     * Verifies EMAIL_VERIFICATION OTP; marks customer as emailVerified=true on success.
     * @return OtpVerificationResult (SUCCESS / INVALID / EXPIRED / MAX_ATTEMPTS_REACHED)
     */
    com.obs.Online_Banking_System.enumDto.OtpVerificationResult verifyEmailOtp(String email, String otp);

    /**
     * Verifies LOGIN_2FA OTP. On success returns the CustomerDto for session creation.
     * @return CustomerDto on SUCCESS, null otherwise (result put in map along with result code)
     */
    java.util.Map<String, Object> verifyLoginOtp(String email, String otp);

    /**
     * Generates a TOTP secret for the customer, saves it (without enabling 2FA yet),
     * and returns the otpauth QR URL and a confirmation message.
     * secretKey is NEVER returned to the caller.
     */
    java.util.Map<String, Object> enableAuthenticator(Long customerId);

    /**
     * Verifies the TOTP OTP during setup. On success marks is2faEnabled=true.
     */
    java.util.Map<String, Object> verifyAuthenticatorSetup(Long customerId, int otp);

    /**
     * Verifies the TOTP OTP during login. On success returns CustomerDto for session creation.
     */
    java.util.Map<String, Object> verifyAuthenticatorLogin(Long customerId, int otp);

}

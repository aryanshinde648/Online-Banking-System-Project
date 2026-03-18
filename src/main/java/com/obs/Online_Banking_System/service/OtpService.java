package com.obs.Online_Banking_System.service;

import com.obs.Online_Banking_System.enumDto.OtpType;
import com.obs.Online_Banking_System.enumDto.OtpVerificationResult;

public interface OtpService {

    /**
     * Deletes any existing OTP for this email+type, generates a new 6-digit OTP,
     * persists it and sends the email. Enforces resend cooldown on resend calls.
     */
    void generateAndSendOtp(String email, OtpType otpType);

    /**
     * Verifies the submitted OTP. Increments attemptCount on failure.
     * Marks the OTP as used and deletes it from DB on success.
     *
     * @return SUCCESS | INVALID | EXPIRED | MAX_ATTEMPTS_REACHED
     */
    OtpVerificationResult verifyOtp(String email, String otp, OtpType otpType);

    /**
     * Resends OTP for the given email+type, enforcing a 60-second cooldown.
     * Throws RuntimeException if called within the cooldown window.
     */
    void resendOtp(String email, OtpType otpType);

    /**
     * Returns seconds remaining before the user can resend again, or 0 if ready.
     */
    long getResendCooldownRemaining(String email, OtpType otpType);
}

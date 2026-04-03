package com.obs.Online_Banking_System.service;

/**
 * Service for Google Authenticator TOTP-based 2FA operations.
 * Handles secret generation, QR code creation, and OTP verification.
 */
public interface TwoFactorService {

    /**
     * Generates a new Base32-encoded TOTP secret key.
     */
    String generateSecret();

    /**
     * Builds the otpauth:// URL used to populate the QR code.
     * Format: otpauth://totp/YourBank:{email}?secret={secret}&issuer=YourBank
     */
    String getQRBarcodeURL(String email, String secret);

    /**
     * Verifies the given TOTP code against the customer's secret.
     * Uses a time window of ±3 steps for clock-drift tolerance.
     *
     * @param secret the Base32-encoded TOTP secret stored for the customer
     * @param otp    the 6-digit code from the authenticator app
     * @return true if OTP is valid, false otherwise
     */
    boolean verifyOTP(String secret, int otp);

    /**
     * Generates a PNG QR code image for the given OTP URL.
     *
     * @param qrUrl  the otpauth:// URL
     * @param width  image width in pixels
     * @param height image height in pixels
     * @return PNG image as a byte array
     */
    byte[] generateQRCodeImage(String qrUrl, int width, int height);
}

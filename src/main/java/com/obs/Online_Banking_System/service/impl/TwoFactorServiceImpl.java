package com.obs.Online_Banking_System.service.impl;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.obs.Online_Banking_System.service.TwoFactorService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TwoFactorServiceImpl implements TwoFactorService {

    private static final String ISSUER     = "YourBank";
    private static final int    WINDOW_SIZE = 3; // ±3 steps = ±90 seconds tolerance

    private final GoogleAuthenticator googleAuthenticator;

    public TwoFactorServiceImpl() {
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setWindowSize(WINDOW_SIZE)
                .build();
        this.googleAuthenticator = new GoogleAuthenticator(config);
    }

    // ── Secret Generation ────────────────────────────────────────────────────

    @Override
    public String generateSecret() {
        GoogleAuthenticatorKey credentials = googleAuthenticator.createCredentials();
        return credentials.getKey();
    }

    // ── QR Code URL ──────────────────────────────────────────────────────────

    @Override
    public String getQRBarcodeURL(String email, String secret) {
        String encodedEmail  = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String encodedIssuer = URLEncoder.encode(ISSUER, StandardCharsets.UTF_8);
        return String.format(
                "otpauth://totp/%s%%3A%s?secret=%s&issuer=%s",
                encodedIssuer, encodedEmail, secret, encodedIssuer
        );
    }

    // ── OTP Verification ─────────────────────────────────────────────────────

    @Override
    public boolean verifyOTP(String secret, int otp) {
        try {
            return googleAuthenticator.authorize(secret, otp);
        } catch (Exception e) {
            log.warn("TOTP verification failed: {}", e.getMessage());
            return false;
        }
    }

    // ── QR Code Image Generation ─────────────────────────────────────────────

    @Override
    public byte[] generateQRCodeImage(String qrUrl, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(qrUrl, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate QR code image: {}", e.getMessage());
            throw new RuntimeException("Failed to generate QR code image", e);
        }
    }
}

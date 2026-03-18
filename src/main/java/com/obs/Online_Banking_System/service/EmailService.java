package com.obs.Online_Banking_System.service;

import com.obs.Online_Banking_System.enumDto.OtpType;

public interface EmailService {

    /**
     * Sends a branded HTML OTP email.
     */
    void sendOtpEmail(String toEmail, String otp, OtpType otpType);

    /**
     * Sends an email with a PDF file attachment.
     *
     * @param toEmail    recipient address
     * @param subject    email subject
     * @param body       plain-text or HTML body
     * @param pdfBytes   raw bytes of the PDF to attach
     * @param fileName   attachment file name (e.g. Statement_John_March.pdf)
     */
    void sendEmailWithAttachment(String toEmail, String subject, String body,
                                 byte[] pdfBytes, String fileName);
}

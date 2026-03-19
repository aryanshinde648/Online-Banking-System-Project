package com.obs.Online_Banking_System.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.obs.Online_Banking_System.enumDto.OtpType;
import com.obs.Online_Banking_System.service.EmailService;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.Attachment;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final Resend resend;
    private final String fromEmail;

    public EmailServiceImpl(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from-email:onboarding@resend.dev}") String fromEmail) {
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp, OtpType otpType) {
        String subject = otpType == OtpType.EMAIL_VERIFICATION
                ? "📧 Verify Your Email — Online Banking System"
                : "🔐 Your Login OTP — Online Banking System";

        String htmlBody = buildEmailHtml(otp, otpType);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject(subject)
                .html(htmlBody)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            log.info("OTP email sent to {} for type {}. Resend ID: {}", toEmail, otpType, data.getId());
        } catch (ResendException e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send OTP email. Please try again.");
        }
    }

    private String buildEmailHtml(String otp, OtpType otpType) {
        String heading = otpType == OtpType.EMAIL_VERIFICATION
                ? "Email Verification"
                : "Two-Factor Authentication";
        String purpose = otpType == OtpType.EMAIL_VERIFICATION
                ? "verify your email address and complete your account registration"
                : "complete your secure login";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                  <title>OTP</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f7ff;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f7ff;padding:40px 20px;">
                    <tr>
                      <td align="center">
                        <table width="560" cellpadding="0" cellspacing="0"
                               style="background:#ffffff;border-radius:16px;box-shadow:0 8px 24px rgba(102,126,234,0.15);overflow:hidden;max-width:560px;width:100%%;">

                          <!-- Header -->
                          <tr>
                            <td style="background:linear-gradient(135deg,#667eea 0%%,#764ba2 100%%);padding:36px 40px;text-align:center;">
                              <div style="font-size:28px;font-weight:800;color:#ffffff;letter-spacing:-0.5px;">
                                🏦 Online Banking System
                              </div>
                              <div style="font-size:13px;color:rgba(255,255,255,0.8);margin-top:6px;letter-spacing:0.3px;">
                                Secure Banking, Trusted Service
                              </div>
                            </td>
                          </tr>

                          <!-- Body -->
                          <tr>
                            <td style="padding:40px 40px 24px;">
                              <h2 style="margin:0 0 8px;font-size:22px;font-weight:700;color:#1e293b;">
                                %s
                              </h2>
                              <p style="margin:0 0 28px;font-size:15px;color:#64748b;line-height:1.6;">
                                Use the code below to %s.
                                This code is valid for <strong>5 minutes</strong>.
                              </p>

                              <!-- OTP Box -->
                              <div style="background:linear-gradient(135deg,#f0f3ff,#e8f0fe);
                                          border:2px dashed #667eea;border-radius:12px;
                                          padding:24px 32px;text-align:center;margin-bottom:28px;">
                                <div style="font-size:11px;font-weight:700;color:#667eea;
                                            text-transform:uppercase;letter-spacing:0.15em;margin-bottom:10px;">
                                  Your One-Time Password
                                </div>
                                <div style="font-size:42px;font-weight:800;color:#1e293b;
                                            letter-spacing:16px;font-family:monospace;">
                                  %s
                                </div>
                                <div style="font-size:12px;color:#94a3b8;margin-top:10px;">
                                  ⏰ Expires in 5 minutes
                                </div>
                              </div>

                              <!-- Warning -->
                              <div style="background:#fff8f0;border-left:4px solid #f59e0b;
                                          border-radius:8px;padding:14px 18px;margin-bottom:24px;">
                                <div style="font-size:13px;font-weight:700;color:#b45309;margin-bottom:4px;">
                                  ⚠️ Security Notice
                                </div>
                                <div style="font-size:13px;color:#92400e;line-height:1.5;">
                                  <strong>Do NOT share this OTP with anyone</strong>, including bank staff.
                                  If you did not request this, please ignore this email and your account
                                  remains secure.
                                </div>
                              </div>

                              <p style="font-size:13px;color:#94a3b8;line-height:1.5;margin:0;">
                                This is an automated message. Please do not reply to this email.
                              </p>
                            </td>
                          </tr>

                          <!-- Footer -->
                          <tr>
                            <td style="background:#f8fafc;padding:20px 40px;border-top:1px solid #e2e8f0;
                                        text-align:center;">
                              <div style="font-size:12px;color:#94a3b8;">
                                © 2026 Online Banking System · All rights reserved
                              </div>
                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(heading, purpose, otp);
    }

    // ── PDF Attachment Email ──────────────────────────────────────────────────

    @Override
    public void sendEmailWithAttachment(String toEmail, String subject, String body,
                                        byte[] pdfBytes, String fileName) {
        
        Attachment attachment = Attachment.builder()
                .fileName(fileName)
                .content(java.util.Base64.getEncoder().encodeToString(pdfBytes))
                .build();

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject(subject)
                .html(buildStatementEmailHtml(body))
                .attachments(attachment)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            log.info("Statement email with attachment sent to {}. Resend ID: {}", toEmail, data.getId());
        } catch (ResendException e) {
            log.error("Failed to send statement email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send statement email: " + e.getMessage());
        }
    }

    private String buildStatementEmailHtml(String bodyText) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"/></head>
                <body style="margin:0;padding:0;background:#f4f7ff;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f7ff;padding:40px 20px;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0"
                             style="background:#ffffff;border-radius:16px;box-shadow:0 8px 24px rgba(28,40,65,0.15);overflow:hidden;max-width:560px;width:100%%;">
                        <tr>
                          <td style="background:linear-gradient(135deg,#1c2841 0%%,#2563eb 100%%);padding:32px 40px;text-align:center;">
                            <div style="font-size:26px;font-weight:800;color:#fff;">🏦 Online Banking System</div>
                            <div style="font-size:12px;color:rgba(255,255,255,0.75);margin-top:4px;">Your Trusted Financial Partner</div>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:32px 40px;">
                            <h2 style="color:#1c2841;margin:0 0 12px;font-size:20px;">📄 Your Bank Statement</h2>
                            <p style="color:#475569;font-size:15px;line-height:1.7;margin:0 0 24px;">%s</p>
                            <div style="background:#eff6ff;border-left:4px solid #2563eb;border-radius:8px;padding:14px 18px;">
                              <p style="color:#1d4ed8;font-size:13px;margin:0;font-weight:500;">
                                📎 Your statement is attached as a PDF to this email.
                              </p>
                            </div>
                            <p style="color:#94a3b8;font-size:12px;margin-top:24px;">
                              This is an automated message. Please do not reply to this email.
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td style="background:#f8fafc;padding:16px 40px;border-top:1px solid #e2e8f0;text-align:center;">
                            <div style="font-size:12px;color:#94a3b8;">© 2026 Online Banking System · All rights reserved</div>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(bodyText);
    }
}

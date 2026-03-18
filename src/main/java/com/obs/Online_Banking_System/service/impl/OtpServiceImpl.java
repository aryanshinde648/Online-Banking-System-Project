package com.obs.Online_Banking_System.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.obs.Online_Banking_System.entity.OtpEntity;
import com.obs.Online_Banking_System.enumDto.OtpType;
import com.obs.Online_Banking_System.enumDto.OtpVerificationResult;
import com.obs.Online_Banking_System.repository.OtpRepository;
import com.obs.Online_Banking_System.service.EmailService;
import com.obs.Online_Banking_System.service.OtpService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;

    @Value("${app.otp.expiry-minutes:5}")
    private int expiryMinutes;

    @Value("${app.otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Value("${app.otp.max-attempts:3}")
    private int maxAttempts;

    private static final SecureRandom RANDOM = new SecureRandom();

    // ── Generate & Send ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public void generateAndSendOtp(String email, OtpType otpType) {
        // Delete any existing OTP for this email+type to enforce single active OTP
        otpRepository.deleteByEmailAndOtpType(email, otpType);

        String otp = generateSixDigitOtp();
        LocalDateTime now = LocalDateTime.now();

        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setOtpType(otpType);
        otpEntity.setExpiryTime(now.plusMinutes(expiryMinutes));
        otpEntity.setUsed(false);
        otpEntity.setAttemptCount(0);
        otpEntity.setLastSentTime(now);

        otpRepository.save(otpEntity);

        emailService.sendOtpEmail(email, otp, otpType);
        log.info("[OTP] Generated {} OTP for {} (expires {})", otpType, email,
                otpEntity.getExpiryTime());
    }

    // ── Verify ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public OtpVerificationResult verifyOtp(String email, String otp, OtpType otpType) {
        Optional<OtpEntity> opt = otpRepository
                .findTopByEmailAndOtpTypeAndIsUsedFalseOrderByExpiryTimeDesc(email, otpType);

        if (opt.isEmpty()) {
            log.warn("[OTP] No active OTP found for {} type={}", email, otpType);
            return OtpVerificationResult.INVALID;
        }

        OtpEntity record = opt.get();

        // Max attempts guard
        if (record.getAttemptCount() >= maxAttempts) {
            log.warn("[OTP] Max attempts reached for {} type={}", email, otpType);
            return OtpVerificationResult.MAX_ATTEMPTS_REACHED;
        }

        // Expiry check
        if (LocalDateTime.now().isAfter(record.getExpiryTime())) {
            log.warn("[OTP] OTP expired for {} type={}", email, otpType);
            otpRepository.deleteByEmailAndOtpType(email, otpType);
            return OtpVerificationResult.EXPIRED;
        }

        // OTP match check
        if (!record.getOtp().equals(otp)) {
            record.setAttemptCount(record.getAttemptCount() + 1);
            otpRepository.save(record);
            log.warn("[OTP] Wrong OTP for {} type={} attempt={}", email, otpType,
                    record.getAttemptCount());

            if (record.getAttemptCount() >= maxAttempts) {
                return OtpVerificationResult.MAX_ATTEMPTS_REACHED;
            }
            return OtpVerificationResult.INVALID;
        }

        // SUCCESS — mark as used and delete
        record.setUsed(true);
        otpRepository.save(record);
        otpRepository.deleteByEmailAndOtpType(email, otpType);

        log.info("[OTP] Verified successfully for {} type={}", email, otpType);
        return OtpVerificationResult.SUCCESS;
    }

    // ── Resend ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void resendOtp(String email, OtpType otpType) {
        long remaining = getResendCooldownRemaining(email, otpType);
        if (remaining > 0) {
            throw new RuntimeException(
                    "Please wait " + remaining + " second(s) before requesting a new OTP.");
        }
        generateAndSendOtp(email, otpType);
    }

    @Override
    public long getResendCooldownRemaining(String email, OtpType otpType) {
        Optional<OtpEntity> opt = otpRepository
                .findTopByEmailAndOtpTypeAndIsUsedFalseOrderByExpiryTimeDesc(email, otpType);
        if (opt.isEmpty()) return 0;

        OtpEntity record = opt.get();
        long elapsed = ChronoUnit.SECONDS.between(record.getLastSentTime(), LocalDateTime.now());
        long remaining = resendCooldownSeconds - elapsed;
        return Math.max(0, remaining);
    }

    // ── Scheduled Cleanup ────────────────────────────────────────────────────

    /** Runs every 10 minutes — purges expired & used OTP records from DB */
    @Scheduled(fixedRate = 600_000)
    @Transactional
    public void cleanupExpiredOtps() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(expiryMinutes);
        otpRepository.deleteExpiredAndUsed(cutoff);
        log.debug("[OTP] Scheduled cleanup executed at {}", LocalDateTime.now());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String generateSixDigitOtp() {
        int code = 100_000 + RANDOM.nextInt(900_000);
        return String.valueOf(code);
    }
}

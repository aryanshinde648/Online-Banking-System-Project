package com.obs.Online_Banking_System.entity;

import java.time.LocalDateTime;

import com.obs.Online_Banking_System.enumDto.OtpType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "otp_records",
        indexes = @Index(name = "idx_otp_email_type", columnList = "email, otp_type"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String otp;

    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type", nullable = false)
    private OtpType otpType;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    @Column(nullable = false)
    private boolean isUsed = false;

    /** Counts wrong verification attempts. Max 3. */
    @Column(nullable = false)
    private int attemptCount = 0;

    /** Used to enforce 60-second resend cooldown. */
    @Column(nullable = false)
    private LocalDateTime lastSentTime;
}

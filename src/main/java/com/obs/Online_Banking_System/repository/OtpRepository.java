package com.obs.Online_Banking_System.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.obs.Online_Banking_System.entity.OtpEntity;
import com.obs.Online_Banking_System.enumDto.OtpType;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, Long> {

    /** Find the latest active (not used) OTP for an email + type */
    Optional<OtpEntity> findTopByEmailAndOtpTypeAndIsUsedFalseOrderByExpiryTimeDesc(
            String email, OtpType otpType);

    /** Delete all OTP records for an email + type (called before reissuing) */
    @Modifying
    @Transactional
    void deleteByEmailAndOtpType(String email, OtpType otpType);

    /** Scheduled cleanup: delete expired & used records older than the given cutoff */
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpEntity o WHERE o.expiryTime < :cutoff OR o.isUsed = true")
    void deleteExpiredAndUsed(@Param("cutoff") LocalDateTime cutoff);
}

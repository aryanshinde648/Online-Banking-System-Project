package com.obs.Online_Banking_System.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.obs.Online_Banking_System.enumDto.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transactionType", nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant timestamp;

    // User remark
    @Column(length = 255)
    private String remark;

    // Remaining balance after transaction
    @Column(name = "remaining_balance", nullable = false)
    private BigDecimal remainingBalance;

    // Many transactions to(in) One Account
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    // For transfer
    private Long targetAccountNumber;

    @Column(name = "sender_account_id")
    private Long senderAccountId;

    @Column(name = "receiver_account_id")
    private Long receiverAccountId;
}

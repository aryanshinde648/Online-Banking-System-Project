package com.obs.Online_Banking_System.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.obs.Online_Banking_System.enumDto.AccountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "Account")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "adharcard", unique = true)
    private Long adharcard;

    @Column(name = "accountNumber", unique = true)
    private Long accountNumber;

    @Column(name = "balance")
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "accountType")
    private AccountType accountType;

    @Column(name = "createdAt" , nullable = false)
    private Instant createdAt;

    @Column(name = "branch")
    private String branch = "All India Branch";

    @Column(name = "ifsc")
    private String ifsc = "ABIN0112233";
    
}

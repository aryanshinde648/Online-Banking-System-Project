package com.obs.Online_Banking_System.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.obs.Online_Banking_System.entity.Account;
import com.obs.Online_Banking_System.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccount(Account account);

    // Newest first — used by the dashboard API
    List<Transaction> findByAccountOrderByTimestampDesc(Account account);
}

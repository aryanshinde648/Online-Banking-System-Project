package com.obs.Online_Banking_System.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.obs.Online_Banking_System.entity.Account;
import com.obs.Online_Banking_System.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction,Long>{
    List<Transaction> findByAccount(Account account);

    Optional<Account> findByAccountNumber(String accountNumber);

}

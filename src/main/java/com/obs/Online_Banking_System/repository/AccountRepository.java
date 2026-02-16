package com.obs.Online_Banking_System.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.obs.Online_Banking_System.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    public Optional<Account> findByAccountNumber(Long accountNumber);

    Account getByAccountNumber(Long accountNumber);

    public Optional<Account> findByAdharcard(Long adharcard);

    Account getByAdharcard(Long adharcard);

    boolean existsByAccountNumber(Long accountNumber);

}

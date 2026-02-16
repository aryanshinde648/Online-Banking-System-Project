package com.obs.Online_Banking_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.obs.Online_Banking_System.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    public Account findByAccountNumber(Long accountNumber);

    public Account findByAdharcard(Long adharcard);

    boolean existsByAccountNumber(Long accountNumber);

}

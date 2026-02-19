package com.obs.Online_Banking_System.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.obs.Online_Banking_System.entity.Account;
import com.obs.Online_Banking_System.entity.Customer;

import jakarta.persistence.LockModeType;

import java.util.List;


@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    public Optional<Account> findByAccountNumber(Long accountNumber);

    Account getByAccountNumber(Long accountNumber);

    public Optional<Account> findByAdharcard(Long adharcard);

    Account getByAdharcard(Long adharcard);

    boolean existsByAccountNumber(Long accountNumber);

    Optional<Account> findByCustomer(Customer customer);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberForUpdate(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.adharcard = :adharcard")
    Optional<Account> findByAdharcardForUpdate(String adharcard);

}

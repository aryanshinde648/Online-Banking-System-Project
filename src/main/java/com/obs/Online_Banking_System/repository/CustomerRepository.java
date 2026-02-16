package com.obs.Online_Banking_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.obs.Online_Banking_System.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long>{
    public Customer findByEmail(String email);

    public Customer findByAdharcard(Long adharcard);
}

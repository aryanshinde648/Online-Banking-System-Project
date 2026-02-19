package com.obs.Online_Banking_System.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.obs.Online_Banking_System.entity.Customer;


@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long>{
    public Optional<Customer> findByEmail(String email);

    Customer getByEmail(String email);

    public Optional<Customer> findByAdharcard(Long adharcard);

    Customer getByAdharcard(Long adharcard);

}

package com.obs.Online_Banking_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.obs.Online_Banking_System.entity.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    public Admin findByEmail(String email);

    public Admin findByAdharcard(Long adharcard);
    
}
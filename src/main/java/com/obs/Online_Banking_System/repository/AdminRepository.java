package com.obs.Online_Banking_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.obs.Online_Banking_System.entity.Admin;
import java.util.List;
import java.util.Optional;


@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    public Admin findByEmail(String email);

    Optional<Admin> getByEmail(String email);

    public Admin findByAdharcard(Long adharcard);

    Optional<Admin> getByAdharcard(Long adharcard);
    
}
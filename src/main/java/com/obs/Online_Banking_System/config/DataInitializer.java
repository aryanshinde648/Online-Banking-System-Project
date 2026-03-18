package com.obs.Online_Banking_System.config;

import com.obs.Online_Banking_System.entity.Admin;
import com.obs.Online_Banking_System.enumDto.AdminRole;
import com.obs.Online_Banking_System.repository.AdminRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String ROOT_EMAIL = "admin@bank.com";
    private static final String ROOT_PASSWORD = "Avenger@1234";

    @Override
    public void run(String... args) {
        // Check if admin already exists by email or aadhar
        Admin existingAdmin = adminRepository.findByEmail(ROOT_EMAIL);
        
        if (existingAdmin == null) {
            Admin root = new Admin();
            root.setFname("Super");
            root.setLname("Admin");
            root.setEmail(ROOT_EMAIL);
            root.setPassword(passwordEncoder.encode(ROOT_PASSWORD));
            root.setPhone("9999999999");
            root.setAddress("Bank Headquarters");
            root.setDob("1990-01-01");
            root.setAdharcard(100000000000L); // placeholder 12-digit Aadhar
            root.setAdminRole(AdminRole.DIRECTOR);

            try {
                adminRepository.save(root);
                log.info("=======================================================");
                log.info("  Default ADMINISTRATIVE admin created.");
                log.info("  Email   : {}", ROOT_EMAIL);
                log.info("  Password: {}", ROOT_PASSWORD);
                log.info("=======================================================");
            } catch (Exception e) {
                log.warn("Failed to create default admin: {}. It may already exist in the database.", e.getMessage());
            }
        } else {
            log.info("Default admin already exists — skipping seed.");
        }
    }
}

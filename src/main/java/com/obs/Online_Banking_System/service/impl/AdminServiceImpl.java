package com.obs.Online_Banking_System.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.obs.Online_Banking_System.dto.AdminDto;
import com.obs.Online_Banking_System.entity.Admin;
import com.obs.Online_Banking_System.mapper.AdminConversion;
import com.obs.Online_Banking_System.repository.AdminRepository;
import com.obs.Online_Banking_System.service.AdminService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminConversion adminConversion;

    @Override
    public ResponseEntity<String> registerAdmin(AdminDto admin) {
        
        Admin newAdmin = adminConversion.toEntity(admin);  

        if (adminRepository.findByEmail(newAdmin.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Admin with email " + newAdmin.getEmail() + " already exists");
        }
        if (adminRepository.findByAdharcard(newAdmin.getAdharcard()) != null) {
            return ResponseEntity.badRequest().body("Admin with Adharcard No. " + newAdmin.getAdharcard() + " already exists");
        }
        
        Admin admin2 = adminRepository.save(newAdmin);

        String name = admin2.getFname() + " " + admin2.getLname();

        return ResponseEntity.ok("Admin " + name + " registered successfully");
    }

    @Override
    public ResponseEntity<AdminDto> getAdminById(Long id) {
        
        Admin admin = adminRepository.findById(id).orElseThrow(() -> new RuntimeException("Admin not found"));

        AdminDto adminDto = adminConversion.toDto(admin);

        return ResponseEntity.ok(adminDto);
    }

    @Override
    public ResponseEntity<String> updateAdmin(Long id, AdminDto admin) {
        
        Admin existingAdmin = adminRepository.findById(id).orElseThrow(() -> new RuntimeException("Admin not found"));

        existingAdmin.setFname(admin.getFname());
        existingAdmin.setLname(admin.getLname());
        existingAdmin.setEmail(admin.getEmail());
        existingAdmin.setPassword(admin.getPassword());

        adminRepository.save(existingAdmin);

        return ResponseEntity.ok("Admin updated successfully");
    }

    @Override
    public ResponseEntity<String> deleteAdmin(Long id) {
        
        Admin existingAdmin = adminRepository.findById(id).orElseThrow(() -> new RuntimeException("Admin not found"));

        adminRepository.delete(existingAdmin);

        return ResponseEntity.ok("Admin deleted successfully");
    }


    @Override
    public ResponseEntity<String> changePassword(Long id, String oldPassword, String newPassword) {
        Admin existingAdmin = adminRepository.findById(id).orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!existingAdmin.getPassword().equals(oldPassword)) {
            return ResponseEntity.badRequest().body("Old password is incorrect");
        }

        existingAdmin.setPassword(newPassword);
        adminRepository.save(existingAdmin);

        return ResponseEntity.ok("Password changed successfully");
    }

    @Override
    public ResponseEntity<AdminDto> getAdminByEmail(String email) {
        Admin admin = adminRepository.findByEmail(email);
        if (admin == null) {
            return ResponseEntity.notFound().build();
        }
        else {
            AdminDto adminDto = adminConversion.toDto(admin);
            return ResponseEntity.ok(adminDto);
        }   
    }

}

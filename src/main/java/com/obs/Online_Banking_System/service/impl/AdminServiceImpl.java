package com.obs.Online_Banking_System.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.obs.Online_Banking_System.dto.AdminDto;
import com.obs.Online_Banking_System.entity.Admin;
import com.obs.Online_Banking_System.enumDto.AdminRole;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<String> registerAdministrative(AdminDto admin) {

        Admin newAdmin = adminConversion.toEntity(admin);

        if (adminRepository.findByEmail(newAdmin.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Admin with email " + newAdmin.getEmail() + " already exists");
        }
        if (adminRepository.findByAdharcard(newAdmin.getAdharcard()) != null) {
            return ResponseEntity.badRequest()
                    .body("Admin with Adharcard No. " + newAdmin.getAdharcard() + " already exists");
        }

        newAdmin.setAdminRole(AdminRole.ADMINISTRATIVE);

        // Hash password before saving
        if (newAdmin.getPassword() != null && !newAdmin.getPassword().isBlank()) {
            newAdmin.setPassword(passwordEncoder.encode(newAdmin.getPassword()));
        }

        Admin admin2 = adminRepository.save(newAdmin);

        String name = admin2.getFname() + " " + admin2.getLname();

        return ResponseEntity.ok("Administrative " + name + " registered successfully");
    }

    @Override
    public ResponseEntity<String> registerAdmin(AdminDto admin) {

        Admin newAdmin = adminConversion.toEntity(admin);

        if (adminRepository.findByEmail(newAdmin.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Admin with email " + newAdmin.getEmail() + " already exists");
        }
        if (adminRepository.findByAdharcard(newAdmin.getAdharcard()) != null) {
            return ResponseEntity.badRequest()
                    .body("Admin with Adharcard No. " + newAdmin.getAdharcard() + " already exists");
        }

        newAdmin.setAdminRole(AdminRole.MANAGER);

        // Hash password before saving
        if (newAdmin.getPassword() != null && !newAdmin.getPassword().isBlank()) {
            newAdmin.setPassword(passwordEncoder.encode(newAdmin.getPassword()));
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
        if (admin.getPassword() != null && !admin.getPassword().isBlank()) {
            existingAdmin.setPassword(passwordEncoder.encode(admin.getPassword()));
        }

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

        if (!passwordEncoder.matches(oldPassword, existingAdmin.getPassword())) {
            return ResponseEntity.badRequest().body("Old password is incorrect");
        }

        existingAdmin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(existingAdmin);

        return ResponseEntity.ok("Password changed successfully");
    }

    @Override
    public ResponseEntity<AdminDto> getAdminByEmail(String email) {
        Admin admin = adminRepository.findByEmail(email);
        if (admin == null) {
            return ResponseEntity.notFound().build();
        } else {
            AdminDto adminDto = adminConversion.toDto(admin);
            return ResponseEntity.ok(adminDto);
        }
    }

    @Override
    public AdminDto register(AdminDto admin) {

        Admin newAdmin = adminConversion.toEntity(admin);

        if (adminRepository.findByEmail(newAdmin.getEmail()) != null) {
            throw new RuntimeException("Admin with email " + newAdmin.getEmail() + " already exists");
        }
        if (adminRepository.findByAdharcard(newAdmin.getAdharcard()) != null) {
            throw new RuntimeException("Admin with Adharcard No. " + newAdmin.getAdharcard() + " already exists");
        }

        newAdmin.setAdminRole(admin.getAdminRole() != null ? admin.getAdminRole() : AdminRole.MANAGER);

        // Hash password before saving
        if (newAdmin.getPassword() != null && !newAdmin.getPassword().isBlank()) {
            newAdmin.setPassword(passwordEncoder.encode(newAdmin.getPassword()));
        }

        Admin saved = adminRepository.save(newAdmin);

        return adminConversion.toDto(saved);
    }

    @Override
    public AdminDto authenticateAdmin(String email, String pass) {
        Admin admin = adminRepository.getByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email"));

        if (!passwordEncoder.matches(pass, admin.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        log.info("Admin Logged success with email: {}" + email);
        return adminConversion.toDto(admin);
    }

    @Override
    public Map<String, Object> athenticateAdminMap(String email, String pass) {
        Map<String, Object> response = new HashMap<>();

        Admin admin = adminRepository.findByEmail(email);

        if (admin == null) {
            response.put("error", "Admin Not Found Invalid Email or Password");
            return response;
        }

        if (!passwordEncoder.matches(pass, admin.getPassword())) {
            response.put("error", "Invalid Email or Password");
            return response;
        }

        log.info("Admin logged in with email: {}" + email);
        response.put("admin", adminConversion.toDto(admin));
        return response;
    }

    @Override
    public List<AdminDto> getAllAdmins() {
        return adminRepository.findAll()
                .stream().map(adminConversion::toDto).collect(Collectors.toList());
    }

    @Override
    public AdminDto fullUpdateAdmin(Long id, AdminDto dto) {
        Admin existing = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        if (dto.getFname() != null)
            existing.setFname(dto.getFname());
        if (dto.getLname() != null)
            existing.setLname(dto.getLname());
        if (dto.getEmail() != null)
            existing.setEmail(dto.getEmail());
        if (dto.getPhone() != null)
            existing.setPhone(dto.getPhone());
        if (dto.getAddress() != null)
            existing.setAddress(dto.getAddress());
        if (dto.getDob() != null)
            existing.setDob(dto.getDob());
        if (dto.getAdminRole() != null)
            existing.setAdminRole(dto.getAdminRole());
        // Only update password if explicitly provided — hash it
        if (dto.getPassword() != null && !dto.getPassword().isBlank())
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        return adminConversion.toDto(adminRepository.save(existing));
    }

}

package com.obs.Online_Banking_System.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.obs.Online_Banking_System.dto.AdminDto;

public interface AdminService {

    public ResponseEntity<String> registerAdmin(AdminDto admin);

    public AdminDto register(AdminDto admin);

    public ResponseEntity<AdminDto> getAdminById(Long id);

    public ResponseEntity<String> updateAdmin(Long id, AdminDto admin);

    public ResponseEntity<String> deleteAdmin(Long id);

    public ResponseEntity<String> changePassword(Long id, String oldPassword, String newPassword);

    public ResponseEntity<AdminDto> getAdminByEmail(String email);

    public AdminDto authenticateAdmin(String email, String pass);

    public ResponseEntity<String> registerAdministrative(AdminDto admin);

    Map<String, Object> athenticateAdminMap(String email, String pass);

    List<AdminDto> getAllAdmins();

    AdminDto fullUpdateAdmin(Long id, AdminDto dto);
}

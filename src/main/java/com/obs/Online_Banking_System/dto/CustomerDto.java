package com.obs.Online_Banking_System.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    private Long customerId;

    @NotBlank
    private String fname;

    @NotBlank
    private String lname;

    @Email
    private String email;

    private String address;

    private String password;

    private String phone;

    private String dob;

    private Long adharcard;

    private String pin;

    private boolean emailVerified;

    private boolean twoFaEnabled;

}

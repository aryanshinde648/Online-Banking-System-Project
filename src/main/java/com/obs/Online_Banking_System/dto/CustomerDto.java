package com.obs.Online_Banking_System.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    private Long id;

    private String fname;
    
    private String lname;
    
    private String email;
    
    private String address;
    
    private String password;
    
    private String phone;
    
    private String dob;

    private Long adharcard;
    
}

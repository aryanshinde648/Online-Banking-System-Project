package com.obs.Online_Banking_System.service;

import java.util.Map;

import com.obs.Online_Banking_System.dto.CustomerDto;

public interface CustomerService {

    public Map<String,Object> registerCustomerMap(CustomerDto customerDto);
    
    public CustomerDto registerCustomer(CustomerDto customerDto);

    public CustomerDto getCustomerById(Long id);

    public CustomerDto getCustomerByEmail(String email);
    
    public CustomerDto updateCustomerByEmail(String email, CustomerDto customerDto);

    public CustomerDto updateCustomerById(Long id, CustomerDto customerDto);

    public String deleteCustomer(Long id);

    public String changePassword(Long id, String oldPassword, String newPassword);

    public CustomerDto getCustomerByAdharcard(Long adharcard);

    CustomerDto athenticateCustomer(String email, String pass);

    Map<String, Object> athenticateCustomerMap(String email, String pass);

}

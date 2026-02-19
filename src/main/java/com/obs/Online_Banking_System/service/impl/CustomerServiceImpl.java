package com.obs.Online_Banking_System.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.obs.Online_Banking_System.dto.CustomerDto;
import com.obs.Online_Banking_System.entity.Customer;
import com.obs.Online_Banking_System.exception.CustomerAlreadyExistsException;
import com.obs.Online_Banking_System.mapper.CustomerConversion;
import com.obs.Online_Banking_System.repository.CustomerRepository;
import com.obs.Online_Banking_System.service.CustomerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerConversion customerConversion;

    @Override
    public Map<String,Object> registerCustomerMap(CustomerDto customerDto) {
        Map<String,Object> response = new HashMap<>();

        Customer newCust = customerConversion.toCustomerEntity(customerDto);

        if (customerRepository.findByAdharcard(customerDto.getAdharcard()).isPresent()) {
            response.put("adhar-error", "Customer with Adharcard No. " + newCust.getAdharcard() + " already exists");
            log.warn("Customer with Adharcard No. " + newCust.getAdharcard() + " already exists");
            return response;
            
        }

        if (customerRepository.findByEmail(customerDto.getEmail()).isPresent()) {
            response.put("email-error", "Customer with Email " + newCust.getEmail() + " already exists");
            log.warn("Customer with Email " + newCust.getEmail() + " already exists");
            return response;
        }

        customerRepository.save(newCust);

        response.put("customer", customerConversion.toCustomerDto(newCust));

        return response;
    }

    @Override
    public CustomerDto registerCustomer(CustomerDto customerDto) {
        
        Customer newCust = customerConversion.toCustomerEntity(customerDto);

        if (customerRepository.findByAdharcard(customerDto.getAdharcard()).isPresent()) {
            throw new RuntimeException("Customer with Adharcard No. " + newCust.getAdharcard() + " already exists");
        }

        customerRepository.save(newCust);

        return customerConversion.toCustomerDto(newCust);
    }

    @Override
    public CustomerDto getCustomerById(Long id) {
        
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));
        
        return customerConversion.toCustomerDto(customer);
    }

    @Override
    public CustomerDto getCustomerByEmail(String email) {

        if (customerRepository.findByEmail(email).isEmpty()) {
            throw new RuntimeException("Customer not found with email: " + email);
        }

        Customer customer = customerRepository.getByEmail(email);

        return customerConversion.toCustomerDto(customer);
    }

    @Override
    public CustomerDto updateCustomerById(Long id, CustomerDto customerDto) {
        Customer existingCustomer = customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));
 
        existingCustomer.setEmail(customerDto.getEmail());
        existingCustomer.setPassword(customerDto.getPassword());
        existingCustomer.setAddress(customerDto.getAddress());
        existingCustomer.setPhone(customerDto.getPhone());

        customerRepository.save(existingCustomer);

        return customerConversion.toCustomerDto(existingCustomer);
    }

    @Override
    public CustomerDto updateCustomerByEmail(String email, CustomerDto customerDto) {

        if (customerRepository.findByEmail(email).isEmpty()) {
            throw new RuntimeException("Customer not found with email: " + email);
        }

        Customer existingCustomer = customerRepository.getByEmail(email);

        existingCustomer.setFname(customerDto.getFname());
        existingCustomer.setLname(customerDto.getLname());
        existingCustomer.setEmail(customerDto.getEmail());
        existingCustomer.setPassword(customerDto.getPassword());
        existingCustomer.setAddress(customerDto.getAddress());
        existingCustomer.setPhone(customerDto.getPhone());


        customerRepository.save(existingCustomer);

        return customerConversion.toCustomerDto(existingCustomer);
    }

    @Override
    public String deleteCustomer(Long id) {
        Customer existingCustomer = customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));

        customerRepository.delete(existingCustomer);

        return "Customer deleted successfully";
    }

    @Override
    public String changePassword(Long id, String oldPassword, String newPassword) {
        Customer existingCustomer = customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!existingCustomer.getPassword().equals(oldPassword)) {
            return "Old password is incorrect";
        }

        existingCustomer.setPassword(newPassword);
        customerRepository.save(existingCustomer);

        return "Password changed successfully";
    }

    @Override
    public CustomerDto getCustomerByAdharcard(Long adharcard) {

        if (customerRepository.findByAdharcard(adharcard).isEmpty()) {
            throw new RuntimeException("Customer not found with adharcard: " + adharcard);
        }

        Customer customer = customerRepository.getByAdharcard(adharcard);
        
        return customerConversion.toCustomerDto(customer);
    }

    @Override
    public CustomerDto athenticateCustomer(String email, String pass) {
        
        Customer customer = customerRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Customer Not Found with email id"));

        if (!customer.getPassword().equals(pass)) {
            throw new RuntimeException("Invalid Email or Password");
        }

        log.info("Customer logged in with email: {}"+ email);
        return customerConversion.toCustomerDto(customer);
    }

    @Override
    public Map<String, Object> athenticateCustomerMap(String email, String pass) {
        Map<String, Object> response = new HashMap<>();
        
        Customer customer = customerRepository.getByEmail(email);

        if (!customer.getPassword().equals(pass)) {
            response.put("error", "Invalid Email or Password");
            return response;
        }

        log.info("Customer logged in with email: {}"+ email);
        response.put("customer", customerConversion.toCustomerDto(customer));
        return response;
    }
    
}

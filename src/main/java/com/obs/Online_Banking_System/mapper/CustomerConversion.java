package com.obs.Online_Banking_System.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.obs.Online_Banking_System.dto.CustomerDto;
import com.obs.Online_Banking_System.entity.Customer;

@Mapper(componentModel = "spring")
public interface CustomerConversion {
    
    @Mapping(source = "customerId", target = "id")
    @Mapping(source = "twoFaEnabled", target = "is2faEnabled")
    Customer toCustomerEntity(CustomerDto customerDto);

    @Mapping(source = "id", target = "customerId")
    @Mapping(source = "is2faEnabled", target = "twoFaEnabled")
    CustomerDto toCustomerDto(Customer customer);

    List<CustomerDto> toCustomerDtoList(List<Customer> customers);

    List<Customer> toCustomerEntityList(List<CustomerDto> customerDtos);
}

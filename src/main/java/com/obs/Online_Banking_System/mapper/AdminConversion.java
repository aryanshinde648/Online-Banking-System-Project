package com.obs.Online_Banking_System.mapper;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.obs.Online_Banking_System.dto.AdminDto;
import com.obs.Online_Banking_System.entity.Admin;

@Mapper(componentModel = "spring")
public interface AdminConversion {
    
    @Mapping(source = "id", target = "adminId")
    public AdminDto toDto(Admin admin);

    @Mapping(source = "adminId", target = "id")
    public Admin toEntity(AdminDto adminDto);

    public List<AdminDto> toDtoList(List<Admin> admins);

    public List<Admin> toEntityList(List<AdminDto> adminDtos);

}

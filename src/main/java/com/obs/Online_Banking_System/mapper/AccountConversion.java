package com.obs.Online_Banking_System.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.obs.Online_Banking_System.dto.AccountDto;
import com.obs.Online_Banking_System.entity.Account;

@Mapper(componentModel = "spring")
public interface AccountConversion {

    Account toAccount(AccountDto accountdDto);

    AccountDto toAccountDto(Account account);

    List<Account> toAccountList(List<AccountDto> accountDtos);

    List<AccountDto> toAccountDtoList(List<Account> accounts);
    
}

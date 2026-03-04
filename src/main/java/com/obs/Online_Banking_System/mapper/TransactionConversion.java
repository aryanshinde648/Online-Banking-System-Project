package com.obs.Online_Banking_System.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.obs.Online_Banking_System.dto.TransactionDto;
import com.obs.Online_Banking_System.entity.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionConversion {

    List<TransactionDto> toTransactionDtoList(List<Transaction> trxList);

    List<Transaction> toTransactionList(List<TransactionDto> trxList);
    
    @Mapping(source = "transactionId", target = "id")
    Transaction toTransaction(TransactionDto dto);

    @Mapping(source = "id", target = "transactionId")
    TransactionDto toTransactionDto(Transaction trx);
}

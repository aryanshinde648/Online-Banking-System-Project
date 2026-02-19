package com.obs.Online_Banking_System.service;

import java.util.List;

import com.obs.Online_Banking_System.dto.TransactionRequestDto;
import com.obs.Online_Banking_System.dto.TransactionResponseDto;

public interface TransactionService {

    String deposit(TransactionRequestDto request, String email);

    String withdraw(TransactionRequestDto request, String email);

    String transfer(TransactionRequestDto request, String email);

    List<TransactionResponseDto> getAllTransactions(String email);
    
}

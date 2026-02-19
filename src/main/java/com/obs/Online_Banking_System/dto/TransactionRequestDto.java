package com.obs.Online_Banking_System.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class TransactionRequestDto {
    
    private BigDecimal amount;
    private Long targetAccountNo;
    private String remark;
}

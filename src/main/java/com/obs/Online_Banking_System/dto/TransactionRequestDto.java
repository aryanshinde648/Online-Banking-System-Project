package com.obs.Online_Banking_System.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDto {

    @NotNull
    @Positive
    private BigDecimal amount;
    private Long targetAccountNo;
    private String remark;
    private String pin; // 6-digit transaction PIN
}

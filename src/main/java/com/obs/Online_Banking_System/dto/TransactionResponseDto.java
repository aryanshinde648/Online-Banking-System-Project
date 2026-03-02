package com.obs.Online_Banking_System.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class TransactionResponseDto {

    private Long transactionId;
    private String type;
    private String amount;
    private String date;

    private String remark;
    private String remainingBalance;

    private String targetAccount;

    private Long senderAccountId;
    private Long receiverAccountId;

    private String senderName;
    private String receiverName;
}

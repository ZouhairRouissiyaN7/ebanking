package com.ebanking.account_service.dtos;

import java.math.BigDecimal;

public record TransferResultDTO(
        String transferReference,
        String sourceAccountId,
        String destinationAccountId,
        BigDecimal amount,
        BigDecimal sourceBalance,
        BigDecimal destinationBalance) {
}

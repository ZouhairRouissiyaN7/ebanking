package com.ebanking.account_service.dtos;

import com.ebanking.account_service.enums.OperationType;

import java.math.BigDecimal;
import java.time.Instant;

public record OperationDTO(
        Long id,
        OperationType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant operationDate,
        String accountId,
        String transferReference) {
}

package com.ebanking.account_service.dtos;

import com.ebanking.account_service.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountDTO(
        String id,
        Long customerId,
        BigDecimal balance,
        AccountStatus status,
        Instant createdAt,
        String type,
        BigDecimal overdraft,
        BigDecimal interestRate) {
}

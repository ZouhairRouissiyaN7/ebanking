package com.ebanking.account_service.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateSavingAccountRequest(
        @NotNull @Positive Long customerId,

        @NotNull @DecimalMin("0.0000") @DecimalMax("1.0000") @Digits(integer = 1, fraction = 4) BigDecimal interestRate) {
}

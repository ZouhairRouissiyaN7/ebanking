package com.ebanking.account_service.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateCurrentAccountRequest(
        @NotNull @Positive Long customerId,

        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal overdraft) {
}

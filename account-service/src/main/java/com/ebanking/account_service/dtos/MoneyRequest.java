package com.ebanking.account_service.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record MoneyRequest(
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 17, fraction = 2) BigDecimal amount,

        String description) {
}

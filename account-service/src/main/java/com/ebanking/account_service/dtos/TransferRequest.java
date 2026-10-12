package com.ebanking.account_service.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank String sourceAccountId,

        @NotBlank String destinationAccountId,

        @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 17, fraction = 2) BigDecimal amount,

        String description) {
}
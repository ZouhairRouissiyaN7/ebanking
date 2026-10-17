package com.ebanking.auth_service.dtos;

import jakarta.validation.constraints.*;

public record CreateCustomerUserRequest(
        @NotBlank String username,

        @NotBlank @Size(min = 8) String password,

        @NotNull @Positive Long customerId) {
}

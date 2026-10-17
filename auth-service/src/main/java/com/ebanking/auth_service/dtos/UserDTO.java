package com.ebanking.auth_service.dtos;

public record UserDTO(
        Long id,
        String username,
        String role,
        Long customerId,
        boolean enabled) {
}

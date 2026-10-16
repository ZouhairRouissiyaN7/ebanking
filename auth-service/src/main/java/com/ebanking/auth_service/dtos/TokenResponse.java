package com.ebanking.auth_service.dtos;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {
}

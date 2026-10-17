package com.ebanking.auth_service.services;

import com.ebanking.auth_service.dtos.TokenResponse;
import com.ebanking.auth_service.entities.AppUser;
import com.ebanking.auth_service.enums.Role;
import com.ebanking.auth_service.repositories.AppUserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TokenService {

        private final JwtEncoder encoder;
        private final AppUserRepository userRepository;
        private final String issuer;
        private final Duration lifetime;

        public TokenService(
                        JwtEncoder encoder,
                        AppUserRepository userRepository,
                        @Value("${app.jwt.issuer}") String issuer,
                        @Value("${app.jwt.access-token-minutes}") long minutes) {
                this.encoder = encoder;
                this.userRepository = userRepository;
                this.issuer = issuer;
                this.lifetime = Duration.ofMinutes(minutes);
        }

        public TokenResponse generate(Authentication authentication) {
                AppUser user = userRepository
                                .findByUsername(authentication.getName())
                                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
                Instant now = Instant.now();

                List<String> roles = authentication.getAuthorities()
                                .stream()
                                .map(authority -> authority.getAuthority())
                                .filter(role -> role.startsWith("ROLE_"))
                                .map(role -> role.substring(5))
                                .toList();

                JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                                .issuer(issuer)
                                .subject(user.getUsername())
                                .audience(List.of("ebanking-api"))
                                .issuedAt(now)
                                .expiresAt(now.plus(lifetime))
                                .id(UUID.randomUUID().toString())
                                .claim("roles", roles)
                                .claim("userId", user.getId());

                if (user.getRole() == Role.CUSTOMER) {
                        if (user.getCustomerId() == null) {
                                throw new IllegalStateException(
                                                "Customer user has no linked customer");
                        }

                        claimsBuilder.claim(
                                        "customerId",
                                        user.getCustomerId());
                }

                JwtClaimsSet claims = claimsBuilder.build();

                JwsHeader header = JwsHeader
                                .with(SignatureAlgorithm.RS256)
                                .keyId("jwt-signing")
                                .build();

                String token = encoder.encode(
                                JwtEncoderParameters.from(header, claims)).getTokenValue();

                return new TokenResponse(
                                token,
                                "Bearer",
                                lifetime.toSeconds());
        }
}
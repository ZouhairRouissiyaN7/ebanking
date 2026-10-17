package com.ebanking.auth_service.services;

import com.ebanking.auth_service.dtos.*;
import com.ebanking.auth_service.entities.AppUser;
import com.ebanking.auth_service.enums.Role;
import com.ebanking.auth_service.repositories.AppUserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserManagementService {

    private final AppUserRepository repository;
    private final PasswordEncoder encoder;

    public UserManagementService(
            AppUserRepository repository,
            PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Transactional
    public UserDTO createCustomerUser(
            CreateCustomerUserRequest request) {
        String username = request.username().trim();

        if (repository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists");
        }

        if (repository.existsByCustomerId(request.customerId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer already has a login account");
        }

        AppUser user = new AppUser(
                username,
                encoder.encode(request.password()),
                Role.CUSTOMER,
                request.customerId());

        try {
            AppUser saved = repository.saveAndFlush(user);

            return new UserDTO(
                    saved.getId(),
                    saved.getUsername(),
                    saved.getRole().name(),
                    saved.getCustomerId(),
                    saved.isEnabled());
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User data conflicts with an existing record",
                    ex);
        }
    }
}

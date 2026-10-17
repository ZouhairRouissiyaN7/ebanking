package com.ebanking.auth_service.web;

import com.ebanking.auth_service.dtos.*;
import com.ebanking.auth_service.services.UserManagementService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/users")
public class UserManagementController {

    private final UserManagementService service;

    public UserManagementController(
            UserManagementService service) {
        this.service = service;
    }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createCustomerUser(
            @Valid @RequestBody CreateCustomerUserRequest request) {
        return service.createCustomerUser(request);
    }
}

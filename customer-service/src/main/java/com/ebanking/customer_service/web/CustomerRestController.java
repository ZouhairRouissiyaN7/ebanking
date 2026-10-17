package com.ebanking.customer_service.web;

import com.ebanking.customer_service.dtos.CustomerDTO;
import com.ebanking.customer_service.dtos.CustomerRequest;
import com.ebanking.customer_service.services.CustomerService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer registration and management")
public class CustomerRestController {

    private final CustomerService service;

    public CustomerRestController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerDTO> getAllCustomers() {
        return service.getAllCustomers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public List<CustomerDTO> searchCustomers(
            @RequestParam(defaultValue = "") String keyword) {
        return service.searchCustomers(keyword);
    }

    @PreAuthorize("@customerAccess.canRead(#id, authentication)")
    @GetMapping("/{id}")
    public CustomerDTO getCustomer(@PathVariable Long id) {
        return service.getCustomer(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDTO createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        return service.createCustomer(request);
    }

    @PutMapping("/{id}")
    public CustomerDTO updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        return service.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable Long id) {
        service.deleteCustomer(id);
    }

    @GetMapping("/me")
    public CustomerDTO getMyProfile(
            @AuthenticationPrincipal Jwt jwt) {
        Long customerId;

        if (jwt.getClaimAsStringList("roles").contains("ADMIN")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Administrators should use customer ID lookup");
        }

        Number claim = jwt.getClaim("customerId");

        if (claim == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No customer is linked to this user");
        }

        customerId = claim.longValue();

        return service.getCustomer(customerId);
    }

}

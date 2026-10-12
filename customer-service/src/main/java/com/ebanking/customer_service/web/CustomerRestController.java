package com.ebanking.customer_service.web;

import com.ebanking.customer_service.dtos.CustomerDTO;
import com.ebanking.customer_service.dtos.CustomerRequest;
import com.ebanking.customer_service.services.CustomerService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/search")
    public List<CustomerDTO> searchCustomers(
            @RequestParam(defaultValue = "") String keyword) {
        return service.searchCustomers(keyword);
    }

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
}

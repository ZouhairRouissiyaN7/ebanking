package com.ebanking.customer_service.services.impl;

import com.ebanking.customer_service.dtos.CustomerDTO;
import com.ebanking.customer_service.dtos.CustomerRequest;
import com.ebanking.customer_service.entities.Customer;
import com.ebanking.customer_service.exceptions.CustomerNotFoundException;
import com.ebanking.customer_service.mappers.CustomerMapper;
import com.ebanking.customer_service.repositories.CustomerRepository;
import com.ebanking.customer_service.services.CustomerService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        return mapper.toDTOList(repository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDTO getCustomer(Long id) {
        return mapper.toDTO(findCustomer(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> searchCustomers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllCustomers();
        }

        String search = keyword.trim();

        List<Customer> customers = repository
                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        search,
                        search);

        return mapper.toDTOList(customers);
    }

    @Override
    public CustomerDTO createCustomer(CustomerRequest request) {

        String email = normalizeEmail(request.email());

        if (repository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "A customer with this email already exists");
        }

        Customer customer = mapper.toEntity(request);

        customer.setName(request.name().trim());
        customer.setEmail(email);

        Customer savedCustomer = repository.saveAndFlush(customer);

        return mapper.toDTO(savedCustomer);
    }

    @Override
    public CustomerDTO updateCustomer(
            Long id,
            CustomerRequest request) {
        Customer customer = findCustomer(id);

        String email = normalizeEmail(request.email());

        if (!customer.getEmail().equalsIgnoreCase(email)
                && repository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "A customer with this email already exists");
        }

        mapper.updateEntity(request, customer);

        customer.setName(request.name().trim());
        customer.setEmail(email);

        Customer updatedCustomer = repository.saveAndFlush(customer);

        return mapper.toDTO(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = findCustomer(id);

        repository.delete(customer);
        repository.flush();
    }

    private Customer findCustomer(Long id) {
        return repository.findById(id)
                .orElseThrow(
                        () -> new CustomerNotFoundException(id));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

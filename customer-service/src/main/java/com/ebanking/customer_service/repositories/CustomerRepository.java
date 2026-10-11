package com.ebanking.customer_service.repositories;

import com.ebanking.customer_service.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    List<Customer> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name,
            String email);

    boolean existsByEmailIgnoreCase(String email);
}
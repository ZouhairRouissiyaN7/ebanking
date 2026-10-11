package com.ebanking.customer_service.exceptions;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super("Customer not found with ID: " + id);
    }
}

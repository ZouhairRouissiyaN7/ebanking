package com.ebanking.account_service.exceptions;

public class InvalidCustomerException extends RuntimeException {

    public InvalidCustomerException(Long id) {
        super("Customer not found: " + id);
    }
}
package com.ebanking.account_service.exceptions;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String id) {
        super("Bank account not found: " + id);
    }
}
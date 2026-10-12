package com.ebanking.account_service.web;

import com.ebanking.account_service.dtos.*;
import com.ebanking.account_service.services.BankAccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class BankAccountRestController {

    private final BankAccountService service;

    @GetMapping
    public List<AccountDTO> getAllAccounts() {
        return service.getAllAccounts();
    }

    @GetMapping("/{id}")
    public AccountDTO getAccount(
            @PathVariable String id) {
        return service.getAccount(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<AccountDTO> getCustomerAccounts(
            @PathVariable Long customerId) {
        return service.getCustomerAccounts(customerId);
    }

    @PostMapping("/current")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createCurrentAccount(
            @Valid @RequestBody CreateCurrentAccountRequest request) {
        return service.createCurrentAccount(request);
    }

    @PostMapping("/saving")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createSavingAccount(
            @Valid @RequestBody CreateSavingAccountRequest request) {
        return service.createSavingAccount(request);
    }
}

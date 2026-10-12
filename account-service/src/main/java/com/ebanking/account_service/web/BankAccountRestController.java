package com.ebanking.account_service.web;

import com.ebanking.account_service.dtos.*;
import com.ebanking.account_service.services.BankAccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

@Tag(name = "Bank Accounts", description = "Management of current and savings accounts")
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

    @PostMapping("/{id}/credit")
    public AccountDTO credit(
            @PathVariable String id,
            @Valid @RequestBody MoneyRequest request,
            @RequestHeader("Idempotency-Key") UUID requestKey) {
        return service.credit(id, request, requestKey);
    }

    @PostMapping("/{id}/debit")
    public AccountDTO debit(
            @PathVariable String id,
            @Valid @RequestBody MoneyRequest request,
            @RequestHeader("Idempotency-Key") UUID requestKey) {
        return service.debit(id, request, requestKey);
    }

    @PostMapping("/transfer")
    public TransferResultDTO transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader("Idempotency-Key") UUID requestKey) {
        return service.transfer(request, requestKey);
    }

    @GetMapping("/{id}/operations")
    public Page<OperationDTO> getOperations(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.getOperations(id, page, size);
    }

}

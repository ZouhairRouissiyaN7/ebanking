package com.ebanking.account_service.web;

import com.ebanking.account_service.dtos.*;
import com.ebanking.account_service.services.BankAccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Tag(name = "Bank Accounts", description = "Management of current and savings accounts")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class BankAccountRestController {

    private final BankAccountService service;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<AccountDTO> getAllAccounts() {
        return service.getAllAccounts();
    }

    @PreAuthorize("@accountAccess.ownsAccount(#id, authentication)")
    @GetMapping("/{id}")
    public AccountDTO getAccount(
            @PathVariable String id) {
        return service.getAccount(id);
    }

    @PreAuthorize("@accountAccess.ownsCustomer(#customerId, authentication)")
    @GetMapping("/customer/{customerId}")
    public List<AccountDTO> getCustomerAccounts(
            @PathVariable Long customerId) {
        return service.getCustomerAccounts(customerId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/current")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createCurrentAccount(
            @Valid @RequestBody CreateCurrentAccountRequest request) {
        return service.createCurrentAccount(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/saving")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createSavingAccount(
            @Valid @RequestBody CreateSavingAccountRequest request) {
        return service.createSavingAccount(request);
    }

    @PreAuthorize("@accountAccess.ownsAccount(#id, authentication)")
    @PostMapping("/{id}/credit")
    public AccountDTO credit(
            @PathVariable String id,
            @Valid @RequestBody MoneyRequest request,
            @RequestHeader("Idempotency-Key") UUID requestKey) {
        return service.credit(id, request, requestKey);
    }

    @PreAuthorize("@accountAccess.ownsAccount(#id, authentication)")
    @PostMapping("/{id}/debit")
    public AccountDTO debit(
            @PathVariable String id,
            @Valid @RequestBody MoneyRequest request,
            @RequestHeader("Idempotency-Key") UUID requestKey) {
        return service.debit(id, request, requestKey);
    }

    @PreAuthorize("@accountAccess.ownsAccount(#request.sourceAccountId(), authentication)")
    @PostMapping("/transfer")
    public TransferResultDTO transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader("Idempotency-Key") UUID requestKey) {
        return service.transfer(request, requestKey);
    }

    @PreAuthorize("@accountAccess.ownsAccount(#id, authentication)")
    @GetMapping("/{id}/operations")
    public Page<OperationDTO> getOperations(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.getOperations(id, page, size);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/me")
    public List<AccountDTO> getMyAccounts(
            @AuthenticationPrincipal Jwt jwt) {
        Number claim = jwt.getClaim("customerId");

        if (claim == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No linked customer");
        }

        return service.getCustomerAccounts(claim.longValue());
    }

}

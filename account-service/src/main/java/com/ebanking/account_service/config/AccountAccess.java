package com.ebanking.account_service.config;

import com.ebanking.account_service.repositories.BankAccountRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("accountAccess")
public class AccountAccess {

    private final BankAccountRepository repository;

    public AccountAccess(BankAccountRepository repository) {
        this.repository = repository;
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream().anyMatch(
                        authority -> authority.getAuthority()
                                .equals("ROLE_ADMIN"));
    }

    public boolean ownsCustomer(
            Long customerId,
            Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }

        Long authenticatedCustomerId = customerId(authentication);

        return authenticatedCustomerId != null
                && authenticatedCustomerId.equals(customerId);
    }

    public boolean ownsAccount(
            String accountId,
            Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }

        Long authenticatedCustomerId = customerId(authentication);

        if (authenticatedCustomerId == null) {
            return false;
        }

        return repository.findById(accountId)
                .map(account -> authenticatedCustomerId.equals(account.getCustomerId()))
                .orElse(false);
    }

    private Long customerId(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwt)) {
            return null;
        }

        if (jwt.getAuthorities().stream().noneMatch(
                authority -> authority.getAuthority()
                        .equals("ROLE_CUSTOMER"))) {
            return null;
        }

        Number claim = jwt.getToken().getClaim("customerId");

        return claim == null ? null : claim.longValue();
    }
}

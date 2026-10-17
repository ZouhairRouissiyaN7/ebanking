package com.ebanking.customer_service.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("customerAccess")
public class CustomerAccess {

    public boolean canRead(
            Long customerId,
            Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwt)) {
            return false;
        }

        if (jwt.getAuthorities().stream().anyMatch(
                authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        if (jwt.getAuthorities().stream().noneMatch(
                authority -> authority.getAuthority().equals("ROLE_CUSTOMER"))) {
            return false;
        }

        Number claim = jwt.getToken().getClaim("customerId");

        return claim != null
                && customerId.equals(claim.longValue());
    }
}

package com.ebanking.account_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "customer-service")
public interface CustomerClient {

    @GetMapping("/api/customers/{id}")
    CustomerResponse getCustomer(@PathVariable("id") Long id);

    record CustomerResponse(
            Long id,
            String name,
            String email) {
    }
}

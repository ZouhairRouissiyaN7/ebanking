
package com.ebanking.account_service.clients;

import com.ebanking.account_service.exceptions.InvalidCustomerException;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
public class CustomerClientFallback
        implements FallbackFactory<CustomerClient> {

    @Override
    public CustomerClient create(Throwable cause) {

        return customerId -> {

            if (cause instanceof FeignException.NotFound) {
                throw new InvalidCustomerException(customerId);
            }

            log.warn(
                    "Customer lookup failed for customer ID {}: {}",
                    customerId,
                    cause.toString());

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Customer Service is temporarily unavailable",
                    cause);
        };
    }
}

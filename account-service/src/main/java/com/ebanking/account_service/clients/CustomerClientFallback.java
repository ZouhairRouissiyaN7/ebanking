
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

        return (customerId, authorization) -> {

            if (cause instanceof FeignException.NotFound) {
                throw new InvalidCustomerException(customerId);
            }

            if (cause instanceof FeignException.Unauthorized) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Customer lookup authentication failed",
                        cause);
            }

            if (cause instanceof FeignException.Forbidden) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Customer lookup access denied",
                        cause);
            }

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Customer Service is temporarily unavailable",
                    cause);
        };

    }
}

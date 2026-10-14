
package com.ebanking.account_service.web;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/circuit-breakers")
@RequiredArgsConstructor
@Profile("dev")
public class CircuitBreakerStatusController {

    private final CircuitBreakerRegistry registry;

    @GetMapping("/customer")
    public Map<String, Object> getCustomerCircuitStatus() {

        CircuitBreaker cb = registry.circuitBreaker("CustomerClientgetCustomerLong");

        var metrics = cb.getMetrics();

        return Map.of(
                "name", cb.getName(),
                "state", cb.getState().name(),
                "failureRate", metrics.getFailureRate(),
                "bufferedCalls", metrics.getNumberOfBufferedCalls(),
                "failedCalls", metrics.getNumberOfFailedCalls(),
                "successfulCalls", metrics.getNumberOfSuccessfulCalls(),
                "notPermittedCalls", metrics.getNumberOfNotPermittedCalls());
    }
}

package com.ebanking.account_service.entities;

import com.ebanking.account_service.enums.OperationType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "account_operations", indexes = {
        @Index(name = "idx_operation_account_date", columnList = "account_id, operation_date")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_operation_request_key", columnNames = "request_key")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "operation_date", nullable = false)
    private Instant operationDate;

    @Column(name = "account_id", nullable = false, length = 36)
    private String accountId;

    @Column(length = 36)
    private String transferReference;

    @Column(name = "request_key", length = 36)
    private String requestKey;

    public AccountOperation(
            OperationType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String accountId,
            String transferReference,
            String requestKey) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.accountId = accountId;
        this.transferReference = transferReference;
        this.requestKey = requestKey;
    }

    @PrePersist
    void onCreate() {
        if (operationDate == null) {
            operationDate = Instant.now();
        }
    }
}
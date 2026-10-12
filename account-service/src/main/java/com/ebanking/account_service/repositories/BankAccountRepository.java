package com.ebanking.account_service.repositories;

import com.ebanking.account_service.entities.BankAccount;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository
                extends JpaRepository<BankAccount, String> {

        List<BankAccount> findByCustomerId(Long customerId);

        boolean existsByCustomerId(Long customerId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT a FROM BankAccount a WHERE a.id = :id")
        Optional<BankAccount> findByIdForUpdate(
                        @Param("id") String id);
}

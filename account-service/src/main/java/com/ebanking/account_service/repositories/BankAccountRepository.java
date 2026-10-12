package com.ebanking.account_service.repositories;

import com.ebanking.account_service.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository
        extends JpaRepository<BankAccount, String> {

    List<BankAccount> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);
}

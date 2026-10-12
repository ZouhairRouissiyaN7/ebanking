package com.ebanking.account_service.repositories;

import com.ebanking.account_service.entities.AccountOperation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountOperationRepository
        extends JpaRepository<AccountOperation, Long> {

    Page<AccountOperation> findByAccountId(
            String accountId,
            Pageable pageable);

    boolean existsByRequestKey(String requestKey);
}

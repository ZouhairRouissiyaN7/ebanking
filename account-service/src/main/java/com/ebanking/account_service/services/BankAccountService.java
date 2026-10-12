package com.ebanking.account_service.services;

import com.ebanking.account_service.dtos.*;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface BankAccountService {

        List<AccountDTO> getAllAccounts();

        AccountDTO getAccount(String id);

        List<AccountDTO> getCustomerAccounts(Long customerId);

        AccountDTO createCurrentAccount(
                        CreateCurrentAccountRequest request);

        AccountDTO createSavingAccount(
                        CreateSavingAccountRequest request);

        AccountDTO credit(
                        String accountId,
                        MoneyRequest request,
                        UUID requestKey);

        AccountDTO debit(
                        String accountId,
                        MoneyRequest request,
                        UUID requestKey);

        TransferResultDTO transfer(
                        TransferRequest request,
                        UUID requestKey);

        Page<OperationDTO> getOperations(
                        String accountId,
                        int page,
                        int size);
}
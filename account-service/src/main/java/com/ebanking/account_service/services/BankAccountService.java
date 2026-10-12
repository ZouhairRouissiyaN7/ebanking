package com.ebanking.account_service.services;

import com.ebanking.account_service.dtos.*;

import java.util.List;

public interface BankAccountService {

    List<AccountDTO> getAllAccounts();

    AccountDTO getAccount(String id);

    List<AccountDTO> getCustomerAccounts(Long customerId);

    AccountDTO createCurrentAccount(
            CreateCurrentAccountRequest request);

    AccountDTO createSavingAccount(
            CreateSavingAccountRequest request);
}
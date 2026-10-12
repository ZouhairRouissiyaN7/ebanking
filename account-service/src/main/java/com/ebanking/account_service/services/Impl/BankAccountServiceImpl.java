package com.ebanking.account_service.services.Impl;

import com.ebanking.account_service.clients.CustomerClient;
import com.ebanking.account_service.dtos.*;
import com.ebanking.account_service.entities.*;
import com.ebanking.account_service.exceptions.*;
import com.ebanking.account_service.mappers.BankAccountMapper;
import com.ebanking.account_service.repositories.BankAccountRepository;
import com.ebanking.account_service.services.BankAccountService;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository repository;
    private final BankAccountMapper mapper;
    private final CustomerClient customerClient;

    @Override
    @Transactional(readOnly = true)
    public List<AccountDTO> getAllAccounts() {
        return mapper.toDTOList(repository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO getAccount(String id) {
        BankAccount account = findAccount(id);
        return mapper.toDTO(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDTO> getCustomerAccounts(Long customerId) {
        return mapper.toDTOList(
                repository.findByCustomerId(customerId));
    }

    @Override
    public AccountDTO createCurrentAccount(
            CreateCurrentAccountRequest request) {
        verifyCustomer(request.customerId());

        CurrentAccount account = new CurrentAccount();

        account.setCustomerId(request.customerId());
        account.setOverdraft(request.overdraft());

        BankAccount saved = repository.saveAndFlush(account);

        return mapper.toDTO(saved);
    }

    @Override
    public AccountDTO createSavingAccount(
            CreateSavingAccountRequest request) {
        verifyCustomer(request.customerId());

        SavingAccount account = new SavingAccount();

        account.setCustomerId(request.customerId());
        account.setInterestRate(request.interestRate());

        BankAccount saved = repository.saveAndFlush(account);

        return mapper.toDTO(saved);
    }

    private BankAccount findAccount(String id) {
        return repository.findById(id)
                .orElseThrow(
                        () -> new AccountNotFoundException(id));
    }

    private void verifyCustomer(Long customerId) {
        try {
            CustomerClient.CustomerResponse customer = customerClient.getCustomer(customerId);

            if (customer == null || customer.id() == null
                    || !customerId.equals(customer.id())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Invalid response from Customer Service");
            }
        } catch (FeignException.NotFound ex) {
            throw new InvalidCustomerException(customerId);
        } catch (FeignException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Customer Service is unavailable",
                    ex);
        }
    }
}
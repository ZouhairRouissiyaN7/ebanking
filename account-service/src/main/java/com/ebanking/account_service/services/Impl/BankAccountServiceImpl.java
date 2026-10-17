package com.ebanking.account_service.services.Impl;

import com.ebanking.account_service.clients.CustomerClient;
import com.ebanking.account_service.dtos.*;
import com.ebanking.account_service.entities.*;
import com.ebanking.account_service.exceptions.*;
import com.ebanking.account_service.mappers.BankAccountMapper;
import com.ebanking.account_service.repositories.BankAccountRepository;
import com.ebanking.account_service.services.BankAccountService;
import com.ebanking.account_service.entities.AccountOperation;
import com.ebanking.account_service.enums.AccountStatus;
import com.ebanking.account_service.enums.OperationType;
import com.ebanking.account_service.exceptions.BankingException;
import com.ebanking.account_service.dtos.MoneyRequest;
import com.ebanking.account_service.dtos.TransferRequest;
import com.ebanking.account_service.dtos.TransferResultDTO;
import com.ebanking.account_service.dtos.OperationDTO;
import com.ebanking.account_service.mappers.AccountOperationMapper;
import com.ebanking.account_service.repositories.AccountOperationRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.*;

import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountServiceImpl implements BankAccountService {

        private final BankAccountRepository repository;
        private final BankAccountMapper mapper;
        private final CustomerClient customerClient;
        private final AccountOperationRepository operationRepository;
        private final AccountOperationMapper operationMapper;

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

                } catch (FeignException.Unauthorized ex) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Customer lookup authentication failed",
                                        ex);

                } catch (FeignException.Forbidden ex) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Not authorized to retrieve customer information",
                                        ex);

                } catch (FeignException ex) {
                        throw new ResponseStatusException(
                                        HttpStatus.SERVICE_UNAVAILABLE,
                                        "Customer Service is unavailable",
                                        ex);
                }
        }

        @Override
        public AccountDTO credit(
                        String accountId,
                        MoneyRequest request,
                        UUID requestKey) {
                BigDecimal amount = validAmount(request.amount());

                BankAccount account = findAccountForUpdate(accountId);

                ensureNotProcessed(requestKey);
                requireActive(account);

                account.setBalance(
                                account.getBalance().add(amount));

                repository.save(account);

                recordOperation(
                                account,
                                OperationType.CREDIT,
                                amount,
                                null,
                                requestKey.toString());

                operationRepository.flush();

                return mapper.toDTO(account);
        }

        @Override
        public AccountDTO debit(
                        String accountId,
                        MoneyRequest request,
                        UUID requestKey) {
                BigDecimal amount = validAmount(request.amount());

                BankAccount account = findAccountForUpdate(accountId);

                ensureNotProcessed(requestKey);
                requireActive(account);
                ensureSufficientFunds(account, amount);

                account.setBalance(
                                account.getBalance().subtract(amount));

                repository.save(account);

                recordOperation(
                                account,
                                OperationType.DEBIT,
                                amount,
                                null,
                                requestKey.toString());

                operationRepository.flush();

                return mapper.toDTO(account);
        }

        @Override
        public TransferResultDTO transfer(
                        TransferRequest request,
                        UUID requestKey) {
                String sourceId = request.sourceAccountId();
                String destinationId = request.destinationAccountId();

                if (sourceId.equals(destinationId)) {
                        throw new BankingException(
                                        HttpStatus.BAD_REQUEST,
                                        "Source and destination must be different");
                }

                BigDecimal amount = validAmount(request.amount());

                String firstId = sourceId.compareTo(destinationId) < 0
                                ? sourceId
                                : destinationId;

                String secondId = sourceId.compareTo(destinationId) < 0
                                ? destinationId
                                : sourceId;

                BankAccount first = findAccountForUpdate(firstId);
                BankAccount second = findAccountForUpdate(secondId);

                BankAccount source = first.getId().equals(sourceId)
                                ? first
                                : second;

                BankAccount destination = first.getId().equals(destinationId)
                                ? first
                                : second;

                ensureNotProcessed(requestKey);
                requireActive(source);
                requireActive(destination);
                ensureSufficientFunds(source, amount);

                source.setBalance(
                                source.getBalance().subtract(amount));

                destination.setBalance(
                                destination.getBalance().add(amount));

                String transferReference = UUID.randomUUID().toString();

                repository.save(source);
                repository.save(destination);

                recordOperation(
                                source,
                                OperationType.DEBIT,
                                amount,
                                transferReference,
                                requestKey.toString());

                recordOperation(
                                destination,
                                OperationType.CREDIT,
                                amount,
                                transferReference,
                                null);

                operationRepository.flush();

                return new TransferResultDTO(
                                transferReference,
                                sourceId,
                                destinationId,
                                amount,
                                source.getBalance(),
                                destination.getBalance());
        }

        @Override
        @Transactional(readOnly = true)
        public Page<OperationDTO> getOperations(
                        String accountId,
                        int page,
                        int size) {
                if (page < 0 || size < 1 || size > 100) {
                        throw new BankingException(
                                        HttpStatus.BAD_REQUEST,
                                        "Page must be >= 0 and size must be between 1 and 100");
                }

                if (!repository.existsById(accountId)) {
                        throw new AccountNotFoundException(accountId);
                }

                Pageable pageable = PageRequest.of(
                                page,
                                size,
                                Sort.by(
                                                Sort.Order.desc("operationDate"),
                                                Sort.Order.desc("id")));

                return operationRepository
                                .findByAccountId(accountId, pageable)
                                .map(operationMapper::toDTO);
        }

        private BankAccount findAccountForUpdate(String id) {
                return repository.findByIdForUpdate(id)
                                .orElseThrow(
                                                () -> new AccountNotFoundException(id));
        }

        private void requireActive(BankAccount account) {
                if (account.getStatus() != AccountStatus.ACTIVATED) {
                        throw new BankingException(
                                        HttpStatus.CONFLICT,
                                        "Account is not activated: " + account.getId());
                }
        }

        private BigDecimal validAmount(BigDecimal amount) {
                if (amount == null
                                || amount.signum() <= 0
                                || amount.scale() > 2
                                || amount.precision() - amount.scale() > 17) {
                        throw new BankingException(
                                        HttpStatus.BAD_REQUEST,
                                        "Amount must be positive with at most two decimal places");
                }

                return amount.setScale(2, RoundingMode.UNNECESSARY);
        }

        private void ensureSufficientFunds(
                        BankAccount account,
                        BigDecimal amount) {
                BigDecimal available = account.getBalance();

                if (account instanceof CurrentAccount current) {
                        available = available.add(current.getOverdraft());
                }

                if (available.compareTo(amount) < 0) {
                        throw new BankingException(
                                        HttpStatus.CONFLICT,
                                        "Insufficient funds");
                }
        }

        private void ensureNotProcessed(UUID requestKey) {
                if (operationRepository.existsByRequestKey(
                                requestKey.toString())) {
                        throw new BankingException(
                                        HttpStatus.CONFLICT,
                                        "This request key has already been processed");
                }
        }

        private void recordOperation(
                        BankAccount account,
                        OperationType type,
                        BigDecimal amount,
                        String transferReference,
                        String requestKey) {
                AccountOperation operation = new AccountOperation(
                                type,
                                amount,
                                account.getBalance(),
                                account.getId(),
                                transferReference,
                                requestKey);

                operationRepository.save(operation);
        }

}
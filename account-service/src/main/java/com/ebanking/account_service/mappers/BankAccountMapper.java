package com.ebanking.account_service.mappers;

import com.ebanking.account_service.dtos.AccountDTO;
import com.ebanking.account_service.entities.*;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    default AccountDTO toDTO(BankAccount account) {
        if (account == null) {
            return null;
        }

        if (account instanceof CurrentAccount current) {
            return toCurrentDTO(current);
        }

        if (account instanceof SavingAccount saving) {
            return toSavingDTO(saving);
        }

        throw new IllegalArgumentException(
                "Unsupported account type");
    }

    @Mapping(target = "type", constant = "CURRENT")
    @Mapping(target = "interestRate", ignore = true)
    AccountDTO toCurrentDTO(CurrentAccount account);

    @Mapping(target = "type", constant = "SAVING")
    @Mapping(target = "overdraft", ignore = true)
    AccountDTO toSavingDTO(SavingAccount account);

    default List<AccountDTO> toDTOList(
            List<BankAccount> accounts) {
        return accounts.stream()
                .map(this::toDTO)
                .toList();
    }
}
package com.ebanking.account_service.mappers;

import com.ebanking.account_service.dtos.OperationDTO;
import com.ebanking.account_service.entities.AccountOperation;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountOperationMapper {

    OperationDTO toDTO(AccountOperation operation);

    List<OperationDTO> toDTOList(
            List<AccountOperation> operations);
}
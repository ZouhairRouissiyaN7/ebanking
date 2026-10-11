package com.ebanking.customer_service.mappers;

import com.ebanking.customer_service.dtos.CustomerDTO;
import com.ebanking.customer_service.dtos.CustomerRequest;
import com.ebanking.customer_service.entities.Customer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerDTO toDTO(Customer customer);

    List<CustomerDTO> toDTOList(List<Customer> customers);

    @Mapping(target = "id", ignore = true)
    Customer toEntity(CustomerRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(
            CustomerRequest request,
            @MappingTarget Customer customer);
}
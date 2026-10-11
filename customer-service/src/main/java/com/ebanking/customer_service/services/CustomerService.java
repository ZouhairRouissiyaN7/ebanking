package com.ebanking.customer_service.services;

import com.ebanking.customer_service.dtos.CustomerDTO;
import com.ebanking.customer_service.dtos.CustomerRequest;

import java.util.List;

public interface CustomerService {

    List<CustomerDTO> getAllCustomers();

    CustomerDTO getCustomer(Long id);

    List<CustomerDTO> searchCustomers(String keyword);

    CustomerDTO createCustomer(CustomerRequest request);

    CustomerDTO updateCustomer(
            Long id,
            CustomerRequest request);

    void deleteCustomer(Long id);
}

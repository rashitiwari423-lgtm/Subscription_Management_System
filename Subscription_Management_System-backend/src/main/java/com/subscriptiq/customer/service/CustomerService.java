package com.subscriptiq.customer.service;

import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.customer.dto.CreateCustomerRequest;
import com.subscriptiq.customer.dto.CustomerResponse;
import com.subscriptiq.customer.dto.UpdateCustomerRequest;
import com.subscriptiq.customer.enums.CustomerStatus;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerResponse createCustomer(CreateCustomerRequest request);

    PagedResponse<CustomerResponse> getCustomers(String search, CustomerStatus status, Pageable pageable);

    CustomerResponse getCustomerById(Long id);

    CustomerResponse getCustomerByUserId(Long userId);

    CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request);

    CustomerResponse updateCustomerStatus(Long id, CustomerStatus status);
}


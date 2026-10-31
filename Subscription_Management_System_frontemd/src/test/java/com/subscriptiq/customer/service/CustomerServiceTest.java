package com.subscriptiq.customer.service;

import com.subscriptiq.common.exception.DuplicateResourceException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.customer.dto.CreateCustomerRequest;
import com.subscriptiq.customer.dto.CustomerResponse;
import com.subscriptiq.customer.dto.UpdateCustomerRequest;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.enums.CustomerStatus;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    @DisplayName("Should successfully create a customer")
    void testCreateCustomer_Success() {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
                .companyName("Acme Corp")
                .contactName("John Doe")
                .email("john@acme.com")
                .phone("1234567890")
                .city("New York")
                .country("USA")
                .build();

        when(customerRepository.existsByEmail("john@acme.com")).thenReturn(false);

        Customer saved = Customer.builder()
                .id(1L)
                .companyName("Acme Corp")
                .contactName("John Doe")
                .email("john@acme.com")
                .phone("1234567890")
                .city("New York")
                .country("USA")
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(customerRepository.save(any(Customer.class))).thenReturn(saved);

        CustomerResponse response = customerService.createCustomer(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCompanyName()).isEqualTo("Acme Corp");
        assertThat(response.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should reject customer creation if email already exists")
    void testCreateCustomer_DuplicateEmail() {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
                .companyName("Acme Corp")
                .contactName("John Doe")
                .email("john@acme.com")
                .build();

        when(customerRepository.existsByEmail("john@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("john@acme.com");
    }

    @Test
    @DisplayName("Should update customer profile successfully")
    void testUpdateCustomer_Success() {
        Customer existing = Customer.builder()
                .id(1L)
                .companyName("Old Corp")
                .contactName("Old Name")
                .email("old@acme.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        UpdateCustomerRequest request = UpdateCustomerRequest.builder()
                .companyName("New Corp")
                .contactName("New Name")
                .email("new@acme.com")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByEmailAndIdNot("new@acme.com", 1L)).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        CustomerResponse response = customerService.updateCustomer(1L, request);

        assertThat(response.getCompanyName()).isEqualTo("New Corp");
        assertThat(response.getEmail()).isEqualTo("new@acme.com");
    }

    @Test
    @DisplayName("Should deactivate customer status")
    void testUpdateCustomerStatus_Deactivate() {
        Customer existing = Customer.builder()
                .id(1L)
                .companyName("Acme Corp")
                .contactName("John")
                .email("john@acme.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        CustomerResponse response = customerService.updateCustomerStatus(1L, CustomerStatus.INACTIVE);

        assertThat(response.getStatus()).isEqualTo(CustomerStatus.INACTIVE);
        verify(customerRepository).save(existing);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when customer ID is invalid")
    void testGetCustomerById_NotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}


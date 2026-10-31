package com.subscriptiq.customer.service;

import com.subscriptiq.common.exception.DuplicateResourceException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.customer.dto.CreateCustomerRequest;
import com.subscriptiq.customer.dto.CustomerResponse;
import com.subscriptiq.customer.dto.UpdateCustomerRequest;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.enums.CustomerStatus;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.user.entity.User;
import com.subscriptiq.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (customerRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Customer with email '" + normalizedEmail + "' already exists");
        }

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
        }

        Customer customer = Customer.builder()
                .user(user)
                .companyName(request.getCompanyName().trim())
                .contactName(request.getContactName().trim())
                .email(normalizedEmail)
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .address(request.getAddress() != null ? request.getAddress().trim() : null)
                .city(request.getCity() != null ? request.getCity().trim() : null)
                .state(request.getState() != null ? request.getState().trim() : null)
                .country(request.getCountry() != null ? request.getCountry().trim() : null)
                .status(CustomerStatus.ACTIVE)
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Created customer id [{}] for company [{}]", saved.getId(), saved.getCompanyName());

        return CustomerResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> getCustomers(String search, CustomerStatus status, Pageable pageable) {
        Specification<Customer> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(search)) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            Specification<Customer> searchSpec = (root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("companyName")), pattern),
                    cb.like(cb.lower(root.get("contactName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
            spec = spec.and(searchSpec);
        }

        if (status != null) {
            Specification<Customer> statusSpec = (root, query, cb) -> cb.equal(root.get("status"), status);
            spec = spec.and(statusSpec);
        }

        Page<CustomerResponse> page = customerRepository.findAll(spec, pageable)
                .map(CustomerResponse::fromEntity);

        return PagedResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return CustomerResponse.fromEntity(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByUserId(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "userId", userId));
        return CustomerResponse.fromEntity(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (customerRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new DuplicateResourceException("Customer with email '" + normalizedEmail + "' already exists");
        }

        customer.setCompanyName(request.getCompanyName().trim());
        customer.setContactName(request.getContactName().trim());
        customer.setEmail(normalizedEmail);
        customer.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        customer.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        customer.setCity(request.getCity() != null ? request.getCity().trim() : null);
        customer.setState(request.getState() != null ? request.getState().trim() : null);
        customer.setCountry(request.getCountry() != null ? request.getCountry().trim() : null);

        Customer updated = customerRepository.save(customer);
        log.info("Updated customer id [{}]", updated.getId());

        return CustomerResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomerStatus(Long id, CustomerStatus status) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        customer.setStatus(status);
        Customer updated = customerRepository.save(customer);
        log.info("Updated customer id [{}] status to [{}]", updated.getId(), status);

        return CustomerResponse.fromEntity(updated);
    }
}


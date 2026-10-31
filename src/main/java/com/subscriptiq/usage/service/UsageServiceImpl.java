package com.subscriptiq.usage.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import com.subscriptiq.usage.dto.CreateUsageRecordRequest;
import com.subscriptiq.usage.dto.UsageRecordResponse;
import com.subscriptiq.usage.entity.UsageRecord;
import com.subscriptiq.usage.repository.UsageRecordRepository;
import com.subscriptiq.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsageServiceImpl implements UsageService {

    private final UsageRecordRepository usageRecordRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UsageCalculationService usageCalculationService;

    @Override
    @Transactional
    public UsageRecordResponse recordUsage(CreateUsageRecordRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + request.getSubscriptionId()));

        if (!subscription.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Subscription with id " + subscription.getId()
                    + " does not belong to customer with id " + customer.getId());
        }

        if (request.getAllocatedQuantity() < 0) {
            throw new BadRequestException("Allocated quantity cannot be negative");
        }
        if (request.getUsedQuantity() < 0) {
            throw new BadRequestException("Used quantity cannot be negative");
        }
        if (request.getUsedQuantity() > request.getAllocatedQuantity()) {
            throw new BadRequestException("usedQuantity (" + request.getUsedQuantity()
                    + ") must not exceed allocatedQuantity (" + request.getAllocatedQuantity() + ")");
        }

        BigDecimal unitCost = request.getUnitCost();
        if (unitCost == null) {
            unitCost = subscription.getUnitPrice();
            if (unitCost == null && subscription.getPlan() != null) {
                unitCost = subscription.getPlan().getPrice();
            }
            if (unitCost == null) {
                unitCost = BigDecimal.ZERO;
            }
        }

        UsageRecord record = UsageRecord.builder()
                .customer(customer)
                .subscription(subscription)
                .resourceType(request.getResourceType())
                .resourceName(request.getResourceName())
                .allocatedQuantity(request.getAllocatedQuantity())
                .usedQuantity(request.getUsedQuantity())
                .unitCost(unitCost)
                .snapshotDate(request.getSnapshotDate())
                .build();

        UsageRecord saved = usageRecordRepository.save(record);
        log.info("Recorded usage record [{}] for customer [{}] and subscription [{}] on [{}]",
                saved.getId(), customer.getId(), subscription.getId(), saved.getSnapshotDate());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UsageRecordResponse getUsageRecordById(Long id, UserPrincipal principal) {
        UsageRecord record = usageRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usage record not found with id: " + id));

        validateCustomerOwnership(record.getCustomer(), principal);
        return mapToResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UsageRecordResponse> getAllUsageRecords(Pageable pageable) {
        Page<UsageRecordResponse> page = usageRecordRepository.findAll(pageable)
                .map(this::mapToResponse);
        return PagedResponse.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UsageRecordResponse> getCustomerUsageRecords(Long customerId, UserPrincipal principal, Pageable pageable) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        validateCustomerOwnership(customer, principal);

        Page<UsageRecordResponse> page = usageRecordRepository.findByCustomerId(customerId, pageable)
                .map(this::mapToResponse);
        return PagedResponse.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UsageRecordResponse> getSubscriptionUsageRecords(Long subscriptionId, UserPrincipal principal, Pageable pageable) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        validateCustomerOwnership(subscription.getCustomer(), principal);

        Page<UsageRecordResponse> page = usageRecordRepository.findBySubscriptionId(subscriptionId, pageable)
                .map(this::mapToResponse);
        return PagedResponse.fromPage(page);
    }

    private void validateCustomerOwnership(Customer customer, UserPrincipal principal) {
        if (principal != null && principal.getRole() == UserRole.CUSTOMER) {
            if (customer.getUser() == null || !customer.getUser().getId().equals(principal.getId())) {
                throw new AccessDeniedException("Access denied: You do not have permission to view other customer records.");
            }
        }
    }

    private UsageRecordResponse mapToResponse(UsageRecord record) {
        Integer unused = usageCalculationService.calculateUnusedQuantity(
                record.getAllocatedQuantity(), record.getUsedQuantity());
        BigDecimal utilization = usageCalculationService.calculateUtilizationPercentage(
                record.getAllocatedQuantity(), record.getUsedQuantity());

        return UsageRecordResponse.builder()
                .id(record.getId())
                .customerId(record.getCustomer() != null ? record.getCustomer().getId() : null)
                .customerCompanyName(record.getCustomer() != null ? record.getCustomer().getCompanyName() : null)
                .subscriptionId(record.getSubscription() != null ? record.getSubscription().getId() : null)
                .subscriptionNumber(record.getSubscription() != null ? record.getSubscription().getSubscriptionNumber() : null)
                .resourceType(record.getResourceType())
                .resourceName(record.getResourceName())
                .allocatedQuantity(record.getAllocatedQuantity())
                .usedQuantity(record.getUsedQuantity())
                .unusedQuantity(unused)
                .utilizationPercentage(utilization)
                .unitCost(record.getUnitCost())
                .snapshotDate(record.getSnapshotDate())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}


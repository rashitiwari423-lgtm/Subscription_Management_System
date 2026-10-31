package com.subscriptiq.subscription.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.quotation.service.QuotationCalculationService;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.subscription.dto.SubscriptionResponse;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import com.subscriptiq.user.enums.UserRole;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final QuotationCalculationService calculationService;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SubscriptionResponse> getSubscriptions(String search, SubscriptionStatus status, Pageable pageable) {
        Specification<Subscription> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate numMatch = cb.like(cb.lower(root.get("subscriptionNumber")), pattern);
                Predicate companyMatch = cb.like(cb.lower(root.get("customer").get("companyName")), pattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("customer").get("email")), pattern);
                predicates.add(cb.or(numMatch, companyMatch, emailMatch));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SubscriptionResponse> page = subscriptionRepository.findAll(spec, pageable).map(this::mapToSubscriptionResponse);
        return PagedResponse.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(Long id, UserPrincipal principal) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        validateCustomerOwnership(subscription.getCustomer(), principal);
        return mapToSubscriptionResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SubscriptionResponse> getCustomerSubscriptions(Long customerId, UserPrincipal principal, Pageable pageable) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        validateCustomerOwnership(customer, principal);

        Page<SubscriptionResponse> page = subscriptionRepository.findByCustomerId(customerId, pageable).map(this::mapToSubscriptionResponse);
        return PagedResponse.fromPage(page);
    }

    @Override
    @Transactional
    public SubscriptionResponse pauseSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new BadRequestException("Subscription is not in ACTIVE status and cannot be paused. Current status: " + subscription.getStatus());
        }

        subscription.setStatus(SubscriptionStatus.PAUSED);
        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Paused subscription [{}]", saved.getSubscriptionNumber());
        return mapToSubscriptionResponse(saved);
    }

    @Override
    @Transactional
    public SubscriptionResponse resumeSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        if (subscription.getStatus() != SubscriptionStatus.PAUSED) {
            throw new BadRequestException("Subscription is not in PAUSED status and cannot be resumed. Current status: " + subscription.getStatus());
        }

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Resumed subscription [{}]", saved.getSubscriptionNumber());
        return mapToSubscriptionResponse(saved);
    }

    @Override
    @Transactional
    public SubscriptionResponse closeSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        if (subscription.getStatus() == SubscriptionStatus.CLOSED) {
            throw new BadRequestException("Subscription is already CLOSED");
        }

        subscription.setStatus(SubscriptionStatus.CLOSED);
        subscription.setEndDate(LocalDate.now());
        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Closed subscription [{}]", saved.getSubscriptionNumber());
        return mapToSubscriptionResponse(saved);
    }

    @Override
    @Transactional
    public SubscriptionResponse renewSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new BadRequestException("Only ACTIVE subscriptions can be renewed. Current status: " + subscription.getStatus());
        }

        LocalDate currentNextBilling = subscription.getNextBillingDate();
        LocalDate updatedNextBilling = calculationService.calculateNextBillingDate(currentNextBilling, subscription.getPlan().getBillingCycle());
        subscription.setNextBillingDate(updatedNextBilling);

        if (subscription.getEndDate() != null) {
            LocalDate updatedEnd = calculationService.calculateNextBillingDate(subscription.getEndDate(), subscription.getPlan().getBillingCycle());
            subscription.setEndDate(updatedEnd);
        }

        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Renewed subscription [{}], updated next billing date to [{}]", saved.getSubscriptionNumber(), updatedNextBilling);
        return mapToSubscriptionResponse(saved);
    }

    private void validateCustomerOwnership(Customer customer, UserPrincipal principal) {
        if (principal != null && principal.getRole() == UserRole.CUSTOMER) {
            if (customer.getUser() == null || !customer.getUser().getId().equals(principal.getId())) {
                throw new AccessDeniedException("Access denied: You do not have permission to view other customer records.");
            }
        }
    }

    private SubscriptionResponse mapToSubscriptionResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .subscriptionNumber(subscription.getSubscriptionNumber())
                .customerId(subscription.getCustomer() != null ? subscription.getCustomer().getId() : null)
                .customerCompanyName(subscription.getCustomer() != null ? subscription.getCustomer().getCompanyName() : null)
                .planId(subscription.getPlan() != null ? subscription.getPlan().getId() : null)
                .planName(subscription.getPlan() != null ? subscription.getPlan().getName() : null)
                .billingCycle(subscription.getPlan() != null ? subscription.getPlan().getBillingCycle() : null)
                .quotationId(subscription.getQuotation() != null ? subscription.getQuotation().getId() : null)
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .nextBillingDate(subscription.getNextBillingDate())
                .quantity(subscription.getQuantity())
                .unitPrice(subscription.getUnitPrice())
                .discountAmount(subscription.getDiscountAmount())
                .taxAmount(subscription.getTaxAmount())
                .totalAmount(subscription.getTotalAmount())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
}


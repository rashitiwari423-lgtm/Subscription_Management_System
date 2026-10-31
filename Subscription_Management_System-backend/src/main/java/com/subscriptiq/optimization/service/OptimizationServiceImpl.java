package com.subscriptiq.optimization.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.optimization.analyzer.OptimizationAnalyzer;
import com.subscriptiq.optimization.dto.AnalyzeUsageRequest;
import com.subscriptiq.optimization.dto.OptimizationInsightResponse;
import com.subscriptiq.optimization.entity.OptimizationInsight;
import com.subscriptiq.optimization.enums.OptimizationStatus;
import com.subscriptiq.optimization.repository.OptimizationInsightRepository;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimizationServiceImpl implements OptimizationService {

    private final OptimizationInsightRepository optimizationInsightRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final OptimizationAnalyzer optimizationAnalyzer;
    private final CostOptimizationService costOptimizationService;

    @Override
    @Transactional
    public List<OptimizationInsightResponse> analyze(AnalyzeUsageRequest request, UserPrincipal principal) {
        List<OptimizationInsight> insights = new ArrayList<>();

        if (request != null && request.getUsageRecordId() != null) {
            UsageRecord usageRecord = usageRecordRepository.findById(request.getUsageRecordId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usage record not found with id: " + request.getUsageRecordId()));
            validateCustomerOwnership(usageRecord.getCustomer(), principal);
            insights.add(optimizationAnalyzer.analyzeUsageRecord(usageRecord, request.getUnitCostOverride()));
        } else if (request != null && request.getSubscriptionId() != null) {
            Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + request.getSubscriptionId()));
            validateCustomerOwnership(subscription.getCustomer(), principal);

            List<UsageRecord> records = usageRecordRepository.findBySubscriptionId(request.getSubscriptionId(), Pageable.unpaged()).getContent();
            for (UsageRecord ur : records) {
                insights.add(optimizationAnalyzer.analyzeUsageRecord(ur, request.getUnitCostOverride()));
            }
        } else if (request != null && request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
            validateCustomerOwnership(customer, principal);

            List<UsageRecord> records = usageRecordRepository.findByCustomerId(request.getCustomerId(), Pageable.unpaged()).getContent();
            for (UsageRecord ur : records) {
                insights.add(optimizationAnalyzer.analyzeUsageRecord(ur, request.getUnitCostOverride()));
            }
        } else {
            List<UsageRecord> allRecords = usageRecordRepository.findAll();
            for (UsageRecord ur : allRecords) {
                BigDecimal unitCostOverride = request != null ? request.getUnitCostOverride() : null;
                insights.add(optimizationAnalyzer.analyzeUsageRecord(ur, unitCostOverride));
            }
        }

        return insights.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OptimizationInsightResponse getInsightById(Long id, UserPrincipal principal) {
        OptimizationInsight insight = optimizationInsightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Optimization insight not found with id: " + id));

        validateCustomerOwnership(insight.getCustomer(), principal);
        return mapToResponse(insight);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OptimizationInsightResponse> getInsights(Pageable pageable) {
        Page<OptimizationInsightResponse> page = optimizationInsightRepository.findAll(pageable)
                .map(this::mapToResponse);
        return PagedResponse.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OptimizationInsightResponse> getCustomerInsights(Long customerId, UserPrincipal principal, Pageable pageable) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        validateCustomerOwnership(customer, principal);

        Page<OptimizationInsightResponse> page = optimizationInsightRepository.findByCustomerId(customerId, pageable)
                .map(this::mapToResponse);
        return PagedResponse.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OptimizationInsightResponse> getSubscriptionInsights(Long subscriptionId, UserPrincipal principal, Pageable pageable) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        validateCustomerOwnership(subscription.getCustomer(), principal);

        Page<OptimizationInsightResponse> page = optimizationInsightRepository.findBySubscriptionId(subscriptionId, pageable)
                .map(this::mapToResponse);
        return PagedResponse.fromPage(page);
    }

    @Override
    @Transactional
    public OptimizationInsightResponse previewInsight(Long id, UserPrincipal principal) {
        OptimizationInsight insight = optimizationInsightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Optimization insight not found with id: " + id));

        validateCustomerOwnership(insight.getCustomer(), principal);

        if (insight.getStatus() == OptimizationStatus.DETECTED || insight.getStatus() == OptimizationStatus.UNDER_REVIEW) {
            insight.setStatus(OptimizationStatus.PREVIEWED);
            insight = optimizationInsightRepository.save(insight);
            log.info("Previewed insight [{}] for customer [{}]", insight.getId(), insight.getCustomer().getId());
        }

        return mapToResponse(insight);
    }

    @Override
    @Transactional
    public OptimizationInsightResponse approveInsight(Long id, UserPrincipal principal) {
        OptimizationInsight insight = optimizationInsightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Optimization insight not found with id: " + id));

        if (insight.getStatus() == OptimizationStatus.REJECTED) {
            throw new BadRequestException("Cannot approve an insight that has been REJECTED.");
        }
        if (insight.getStatus() == OptimizationStatus.EXECUTED) {
            throw new BadRequestException("Insight has already been EXECUTED.");
        }

        // Section 8: Approval prepares the decision and records approval without mutating subscriptions or billing
        insight.setStatus(OptimizationStatus.APPROVED);
        OptimizationInsight saved = optimizationInsightRepository.save(insight);
        log.info("Approved optimization insight [{}] for customer [{}] by principal [{}]",
                saved.getId(), saved.getCustomer().getId(), principal != null ? principal.getEmail() : "system");

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public OptimizationInsightResponse rejectInsight(Long id, String reason, UserPrincipal principal) {
        OptimizationInsight insight = optimizationInsightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Optimization insight not found with id: " + id));

        if (insight.getStatus() == OptimizationStatus.EXECUTED) {
            throw new BadRequestException("Cannot reject an insight that has already been EXECUTED.");
        }

        insight.setStatus(OptimizationStatus.REJECTED);
        if (reason != null && !reason.trim().isEmpty()) {
            String currentNotes = insight.getNotes() != null ? insight.getNotes() + " | Rejection reason: " : "Rejection reason: ";
            insight.setNotes(currentNotes + reason.trim());
        }

        OptimizationInsight saved = optimizationInsightRepository.save(insight);
        log.info("Rejected optimization insight [{}] for customer [{}] by principal [{}]",
                saved.getId(), saved.getCustomer().getId(), principal != null ? principal.getEmail() : "system");

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OptimizationInsightResponse> getHistory(Pageable pageable) {
        Page<OptimizationInsightResponse> page = optimizationInsightRepository
                .findByStatusIn(List.of(OptimizationStatus.APPROVED, OptimizationStatus.REJECTED, OptimizationStatus.EXECUTED), pageable)
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

    private OptimizationInsightResponse mapToResponse(OptimizationInsight insight) {
        BigDecimal currentMonthlyCost = costOptimizationService.calculateCurrentMonthlyCost(
                insight.getAllocatedQuantity(), insight.getUnitCost());

        String proposedAction = optimizationAnalyzer.generateProposedAction(
                insight.getRecommendation(),
                insight.getResourceName(),
                insight.getUnusedQuantity(),
                insight.getPotentialMonthlySavings(),
                insight.getUtilizationPercentage());

        return OptimizationInsightResponse.builder()
                .id(insight.getId())
                .customerId(insight.getCustomer() != null ? insight.getCustomer().getId() : null)
                .customerCompanyName(insight.getCustomer() != null ? insight.getCustomer().getCompanyName() : null)
                .subscriptionId(insight.getSubscription() != null ? insight.getSubscription().getId() : null)
                .subscriptionNumber(insight.getSubscription() != null ? insight.getSubscription().getSubscriptionNumber() : null)
                .usageRecordId(insight.getUsageRecord() != null ? insight.getUsageRecord().getId() : null)
                .resourceName(insight.getResourceName())
                .allocatedQuantity(insight.getAllocatedQuantity())
                .usedQuantity(insight.getUsedQuantity())
                .unusedQuantity(insight.getUnusedQuantity())
                .utilizationPercentage(insight.getUtilizationPercentage())
                .unitCost(insight.getUnitCost())
                .currentMonthlyCost(currentMonthlyCost)
                .potentialMonthlySavings(insight.getPotentialMonthlySavings())
                .potentialAnnualSavings(insight.getPotentialAnnualSavings())
                .recommendation(insight.getRecommendation())
                .proposedAction(proposedAction)
                .status(insight.getStatus())
                .notes(insight.getNotes())
                .createdAt(insight.getCreatedAt())
                .updatedAt(insight.getUpdatedAt())
                .build();
    }
}


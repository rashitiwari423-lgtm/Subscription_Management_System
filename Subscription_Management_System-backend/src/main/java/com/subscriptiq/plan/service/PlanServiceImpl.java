package com.subscriptiq.plan.service;

import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.plan.dto.CreatePlanRequest;
import com.subscriptiq.plan.dto.PlanResponse;
import com.subscriptiq.plan.dto.UpdatePlanRequest;
import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.enums.PlanStatus;
import com.subscriptiq.plan.repository.PlanRepository;
import com.subscriptiq.product.entity.ProductVariant;
import com.subscriptiq.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final ProductVariantRepository variantRepository;
    private final PlanRepository planRepository;

    @Override
    @Transactional
    public PlanResponse createPlan(Long variantId, CreatePlanRequest request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", variantId));

        String currency = StringUtils.hasText(request.getCurrency())
                ? request.getCurrency().trim().toUpperCase()
                : "INR";

        Plan plan = Plan.builder()
                .productVariant(variant)
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .billingCycle(request.getBillingCycle())
                .price(request.getPrice())
                .currency(currency)
                .trialDays(request.getTrialDays() != null ? request.getTrialDays() : 0)
                .status(PlanStatus.ACTIVE)
                .build();

        Plan saved = planRepository.save(plan);
        log.info("Created plan [{}] with price [{} {}] under variant id [{}]",
                saved.getName(), saved.getCurrency(), saved.getPrice(), variantId);

        return PlanResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public com.subscriptiq.common.response.PagedResponse<PlanResponse> getAllPlans(PlanStatus status, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Plan> page = status != null
                ? planRepository.findByStatus(status, pageable)
                : planRepository.findAll(pageable);
        return com.subscriptiq.common.response.PagedResponse.fromPage(page.map(PlanResponse::fromEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponse> getPlansByVariantId(Long variantId, boolean activeOnly) {
        if (!variantRepository.existsById(variantId)) {
            throw new ResourceNotFoundException("ProductVariant", "id", variantId);
        }

        List<Plan> plans = activeOnly
                ? planRepository.findByProductVariantIdAndStatus(variantId, PlanStatus.ACTIVE)
                : planRepository.findByProductVariantId(variantId);

        return plans.stream()
                .map(PlanResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponse getPlanById(Long id, boolean activeOnly) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", id));

        if (activeOnly && plan.getStatus() != PlanStatus.ACTIVE) {
            throw new ResourceNotFoundException("Plan", "id", id);
        }

        return PlanResponse.fromEntity(plan);
    }

    @Override
    @Transactional
    public PlanResponse updatePlan(Long id, UpdatePlanRequest request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", id));

        plan.setName(request.getName().trim());
        plan.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        plan.setBillingCycle(request.getBillingCycle());
        plan.setPrice(request.getPrice());

        if (StringUtils.hasText(request.getCurrency())) {
            plan.setCurrency(request.getCurrency().trim().toUpperCase());
        }

        if (request.getTrialDays() != null) {
            plan.setTrialDays(request.getTrialDays());
        }

        Plan updated = planRepository.save(plan);
        log.info("Updated plan [{}] with id [{}]", updated.getName(), updated.getId());

        return PlanResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public PlanResponse updatePlanStatus(Long id, PlanStatus status) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", id));

        plan.setStatus(status);
        Plan updated = planRepository.save(plan);
        log.info("Updated plan [{}] status to [{}]", updated.getId(), status);

        return PlanResponse.fromEntity(updated);
    }
}


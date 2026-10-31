package com.subscriptiq.plan.service;

import com.subscriptiq.plan.dto.CreatePlanRequest;
import com.subscriptiq.plan.dto.PlanResponse;
import com.subscriptiq.plan.dto.UpdatePlanRequest;
import com.subscriptiq.plan.enums.PlanStatus;

import java.util.List;

public interface PlanService {

    PlanResponse createPlan(Long variantId, CreatePlanRequest request);

    com.subscriptiq.common.response.PagedResponse<PlanResponse> getAllPlans(PlanStatus status, org.springframework.data.domain.Pageable pageable);

    List<PlanResponse> getPlansByVariantId(Long variantId, boolean activeOnly);

    PlanResponse getPlanById(Long id, boolean activeOnly);

    PlanResponse updatePlan(Long id, UpdatePlanRequest request);

    PlanResponse updatePlanStatus(Long id, PlanStatus status);
}


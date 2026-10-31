package com.subscriptiq.optimization.service;

import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.optimization.dto.AnalyzeUsageRequest;
import com.subscriptiq.optimization.dto.OptimizationInsightResponse;
import com.subscriptiq.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OptimizationService {

    List<OptimizationInsightResponse> analyze(AnalyzeUsageRequest request, UserPrincipal principal);

    OptimizationInsightResponse getInsightById(Long id, UserPrincipal principal);

    PagedResponse<OptimizationInsightResponse> getInsights(Pageable pageable);

    PagedResponse<OptimizationInsightResponse> getCustomerInsights(Long customerId, UserPrincipal principal, Pageable pageable);

    PagedResponse<OptimizationInsightResponse> getSubscriptionInsights(Long subscriptionId, UserPrincipal principal, Pageable pageable);

    OptimizationInsightResponse previewInsight(Long id, UserPrincipal principal);

    OptimizationInsightResponse approveInsight(Long id, UserPrincipal principal);

    OptimizationInsightResponse rejectInsight(Long id, String reason, UserPrincipal principal);

    PagedResponse<OptimizationInsightResponse> getHistory(Pageable pageable);
}


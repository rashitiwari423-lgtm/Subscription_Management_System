package com.subscriptiq.ai.service;

import com.subscriptiq.ai.dto.AiAskRequest;
import com.subscriptiq.ai.dto.AiAskResponse;
import com.subscriptiq.ai.dto.AiInsightResponse;
import com.subscriptiq.ai.dto.BusinessOverviewResult;
import com.subscriptiq.ai.dto.ExpiringSubscriptionResult;
import com.subscriptiq.ai.dto.LatePaymentResult;
import com.subscriptiq.ai.dto.OptimizationSummaryResult;
import com.subscriptiq.ai.dto.OverduePaymentResult;
import com.subscriptiq.ai.dto.PlanPerformanceResult;
import com.subscriptiq.ai.dto.RevenueSummaryResult;
import com.subscriptiq.ai.dto.SubscriptionSummaryResult;
import com.subscriptiq.ai.dto.UnusedResourceResult;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.ai.entity.AiQueryLog;
import com.subscriptiq.ai.enums.AiInsightStatus;
import com.subscriptiq.ai.enums.AiIntent;
import com.subscriptiq.ai.enums.AiRecommendedAction;
import com.subscriptiq.ai.enums.AiSeverity;
import com.subscriptiq.ai.repository.AiQueryLogRepository;
import com.subscriptiq.ai.service.classifier.AiIntentClassifier;
import com.subscriptiq.ai.service.explanation.AiExplanationService;
import com.subscriptiq.ai.service.tools.BusinessOverviewTool;
import com.subscriptiq.ai.service.tools.ExpiringSubscriptionTool;
import com.subscriptiq.ai.service.tools.LatePaymentTool;
import com.subscriptiq.ai.service.tools.OptimizationSummaryTool;
import com.subscriptiq.ai.service.tools.OverduePaymentTool;
import com.subscriptiq.ai.service.tools.PlanPerformanceTool;
import com.subscriptiq.ai.service.tools.RevenueSummaryTool;
import com.subscriptiq.ai.service.tools.SubscriptionSummaryTool;
import com.subscriptiq.ai.service.tools.UnusedResourceTool;
import com.subscriptiq.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCommandServiceImpl implements AiCommandService {

    private final AiIntentClassifier aiIntentClassifier;
    private final AiExplanationService aiExplanationService;
    private final AiInsightService aiInsightService;
    private final AiQueryLogRepository aiQueryLogRepository;

    private final BusinessOverviewTool businessOverviewTool;
    private final OverduePaymentTool overduePaymentTool;
    private final ExpiringSubscriptionTool expiringSubscriptionTool;
    private final LatePaymentTool latePaymentTool;
    private final RevenueSummaryTool revenueSummaryTool;
    private final SubscriptionSummaryTool subscriptionSummaryTool;
    private final UnusedResourceTool unusedResourceTool;
    private final OptimizationSummaryTool optimizationSummaryTool;
    private final PlanPerformanceTool planPerformanceTool;

    @Override
    @Transactional
    public AiAskResponse ask(AiAskRequest request, UserPrincipal principal) {
        String question = request.getQuestion();
        AiIntent intent = aiIntentClassifier.classifyIntent(question);
        log.info("Processing AI command question: [{}] -> detected intent: [{}]", question, intent);

        List<AiInsightResponse> insights = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        String summary;
        String outcome = "SUCCESS";

        switch (intent) {
            case BUSINESS_OVERVIEW -> {
                BusinessOverviewResult result = businessOverviewTool.analyze();
                summary = aiExplanationService.explain(intent, result);

                for (AiInsightResponse item : result.getInsights()) {
                    AiInsight entity = persistInsight(
                            item.getType(), item.getTitle(), item.getDescription(),
                            item.getSeverity(), item.getAffectedCustomers(), item.getAffectedSubscriptions(),
                            item.getAffectedInvoices(), item.getPotentialRevenue(), item.getPotentialSavings(),
                            item.getRecommendedAction());
                    insights.add(mapEntityToResponse(entity));

                    if (item.getRecommendedAction() != null && item.getRecommendedAction() != AiRecommendedAction.NO_ACTION) {
                        recommendations.add(item.getRecommendedAction().name());
                    }
                }
            }
            case OVERDUE_PAYMENTS -> {
                OverduePaymentResult result = overduePaymentTool.analyze();
                summary = aiExplanationService.explain(intent, result);

                AiInsight entity = persistInsight(
                        intent.name(),
                        result.getCount() > 0 ? "Overdue payments detected" : "No overdue payments",
                        summary,
                        result.getSeverity(),
                        result.getTopAffectedCustomers().size(),
                        0,
                        result.getCount(),
                        result.getTotalAmount(),
                        BigDecimal.ZERO,
                        result.getRecommendedAction()
                );
                insights.add(mapEntityToResponse(entity));

                if (result.getCount() > 0 && result.getRecommendedAction() != null) {
                    recommendations.add(result.getRecommendedAction().name());
                }
            }
            case EXPIRING_SUBSCRIPTIONS -> {
                ExpiringSubscriptionResult result = expiringSubscriptionTool.analyze();
                summary = aiExplanationService.explain(intent, result);

                AiInsight entity = persistInsight(
                        intent.name(),
                        result.getCount() > 0 ? "Subscriptions expiring soon" : "No subscriptions expiring",
                        summary,
                        result.getSeverity(),
                        result.getCustomers().size(),
                        result.getCount(),
                        0,
                        result.getPotentialRevenue(),
                        BigDecimal.ZERO,
                        result.getRecommendedAction()
                );
                insights.add(mapEntityToResponse(entity));

                if (result.getCount() > 0 && result.getRecommendedAction() != null) {
                    recommendations.add(result.getRecommendedAction().name());
                }
            }
            case LATE_PAYMENTS -> {
                LatePaymentResult result = latePaymentTool.analyze();
                summary = aiExplanationService.explain(intent, result);

                AiInsight entity = persistInsight(
                        intent.name(),
                        result.getLatePaymentCount() > 0 ? "Repeated late payments detected" : "Healthy payment timeliness",
                        summary,
                        result.getSeverity(),
                        result.getAffectedCustomerCount(),
                        0,
                        result.getLatePaymentCount(),
                        result.getTotalAmountInvolved(),
                        BigDecimal.ZERO,
                        result.getRecommendedAction()
                );
                insights.add(mapEntityToResponse(entity));

                if (result.getLatePaymentCount() > 0 && result.getRecommendedAction() != null) {
                    recommendations.add(result.getRecommendedAction().name());
                }
            }
            case REVENUE_SUMMARY -> {
                RevenueSummaryResult result = revenueSummaryTool.analyze();
                summary = aiExplanationService.explain(intent, result);
            }
            case SUBSCRIPTION_SUMMARY -> {
                SubscriptionSummaryResult result = subscriptionSummaryTool.analyze();
                summary = aiExplanationService.explain(intent, result);
            }
            case UNUSED_RESOURCES -> {
                UnusedResourceResult result = unusedResourceTool.analyze();
                summary = aiExplanationService.explain(intent, result);

                if (result.getTotalUnused() > 0) {
                    AiInsight entity = persistInsight(
                            intent.name(),
                            "Underutilized resource capacity detected",
                            summary,
                            result.getSeverity(),
                            result.getAffectedCustomerCount(),
                            0,
                            0,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            result.getRecommendedAction()
                    );
                    insights.add(mapEntityToResponse(entity));
                    recommendations.add(result.getRecommendedAction().name());
                }
            }
            case OPTIMIZATION_SUMMARY -> {
                OptimizationSummaryResult result = optimizationSummaryTool.analyze();
                summary = aiExplanationService.explain(intent, result);

                if (result.getPotentialMonthlySavings().compareTo(BigDecimal.ZERO) > 0) {
                    AiInsight entity = persistInsight(
                            intent.name(),
                            "Cost optimization potential identified",
                            summary,
                            result.getSeverity(),
                            result.getAffectedCustomerCount(),
                            0,
                            0,
                            BigDecimal.ZERO,
                            result.getPotentialMonthlySavings(),
                            result.getRecommendedAction()
                    );
                    insights.add(mapEntityToResponse(entity));
                    recommendations.add(result.getRecommendedAction().name());
                }
            }
            case PLAN_PERFORMANCE -> {
                PlanPerformanceResult result = planPerformanceTool.analyze();
                summary = aiExplanationService.explain(intent, result);
            }
            case UNSUPPORTED_QUERY -> {
                summary = aiExplanationService.explain(intent, null);
                outcome = "UNSUPPORTED_OR_BLOCKED";
            }
            default -> {
                summary = "Unsupported query.";
                outcome = "UNSUPPORTED";
            }
        }

        // Section 17: Record audit query log
        try {
            aiQueryLogRepository.save(AiQueryLog.builder()
                    .userId(principal != null ? principal.getId() : null)
                    .userEmail(principal != null ? principal.getEmail() : "anonymous")
                    .question(question)
                    .detectedIntent(intent.name())
                    .outcome(outcome)
                    .build());
        } catch (Exception e) {
            log.error("Failed to write AI audit query log: {}", e.getMessage());
        }

        return AiAskResponse.builder()
                .question(question)
                .detectedIntent(intent)
                .summary(summary)
                .insights(insights)
                .recommendations(recommendations.stream().distinct().toList())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private AiInsight persistInsight(
            String type, String title, String description, AiSeverity severity,
            Integer affectedCustomers, Integer affectedSubscriptions, Integer affectedInvoices,
            BigDecimal potentialRevenue, BigDecimal potentialSavings, AiRecommendedAction recommendedAction) {

        AiInsight insight = AiInsight.builder()
                .type(type)
                .title(title)
                .description(description)
                .severity(severity != null ? severity : AiSeverity.INFO)
                .affectedCustomers(affectedCustomers != null ? affectedCustomers : 0)
                .affectedSubscriptions(affectedSubscriptions != null ? affectedSubscriptions : 0)
                .affectedInvoices(affectedInvoices != null ? affectedInvoices : 0)
                .potentialRevenue(potentialRevenue != null ? potentialRevenue : BigDecimal.ZERO)
                .potentialSavings(potentialSavings != null ? potentialSavings : BigDecimal.ZERO)
                .recommendedAction(recommendedAction)
                .status(AiInsightStatus.NEW)
                .build();

        return aiInsightService.saveInsight(insight);
    }

    private AiInsightResponse mapEntityToResponse(AiInsight insight) {
        return AiInsightResponse.builder()
                .id(insight.getId())
                .type(insight.getType())
                .title(insight.getTitle())
                .description(insight.getDescription())
                .severity(insight.getSeverity())
                .affectedCustomers(insight.getAffectedCustomers())
                .affectedSubscriptions(insight.getAffectedSubscriptions())
                .affectedInvoices(insight.getAffectedInvoices())
                .potentialRevenue(insight.getPotentialRevenue())
                .potentialSavings(insight.getPotentialSavings())
                .recommendedAction(insight.getRecommendedAction())
                .status(insight.getStatus())
                .createdAt(insight.getCreatedAt())
                .updatedAt(insight.getUpdatedAt())
                .build();
    }
}


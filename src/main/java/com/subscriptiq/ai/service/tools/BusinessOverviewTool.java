package com.subscriptiq.ai.service.tools;

import com.subscriptiq.ai.dto.AiInsightResponse;
import com.subscriptiq.ai.dto.BusinessOverviewResult;
import com.subscriptiq.ai.dto.ExpiringSubscriptionResult;
import com.subscriptiq.ai.dto.LatePaymentResult;
import com.subscriptiq.ai.dto.OptimizationSummaryResult;
import com.subscriptiq.ai.dto.OverduePaymentResult;
import com.subscriptiq.ai.dto.UnusedResourceResult;
import com.subscriptiq.ai.enums.AiInsightStatus;
import com.subscriptiq.ai.enums.AiRecommendedAction;
import com.subscriptiq.ai.enums.AiSeverity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessOverviewTool {

    private final OverduePaymentTool overduePaymentTool;
    private final ExpiringSubscriptionTool expiringSubscriptionTool;
    private final LatePaymentTool latePaymentTool;
    private final OptimizationSummaryTool optimizationSummaryTool;
    private final UnusedResourceTool unusedResourceTool;

    public BusinessOverviewResult analyze() {
        List<AiInsightResponse> insights = new ArrayList<>();
        AiSeverity overallSeverity = AiSeverity.INFO;

        // 1. Check Overdue Invoices
        OverduePaymentResult overdueResult = overduePaymentTool.analyze();
        if (overdueResult.getCount() > 0) {
            if (overdueResult.getSeverity() == AiSeverity.CRITICAL) {
                overallSeverity = AiSeverity.CRITICAL;
            } else if (overallSeverity != AiSeverity.CRITICAL && overdueResult.getSeverity() == AiSeverity.WARNING) {
                overallSeverity = AiSeverity.WARNING;
            }

            insights.add(AiInsightResponse.builder()
                    .type("OVERDUE_PAYMENTS")
                    .title("Overdue payments detected")
                    .description(String.format("%d overdue invoices totaling INR %s (oldest due date: %s)",
                            overdueResult.getCount(), overdueResult.getTotalAmount(), overdueResult.getOldestDueDate()))
                    .severity(overdueResult.getSeverity())
                    .affectedCustomers(overdueResult.getTopAffectedCustomers().size())
                    .affectedInvoices(overdueResult.getCount())
                    .potentialRevenue(overdueResult.getTotalAmount())
                    .potentialSavings(BigDecimal.ZERO)
                    .recommendedAction(AiRecommendedAction.FOLLOW_UP_OVERDUE_CUSTOMERS)
                    .status(AiInsightStatus.NEW)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        // 2. Check Expiring Subscriptions
        ExpiringSubscriptionResult expiringResult = expiringSubscriptionTool.analyze();
        if (expiringResult.getCount() > 0) {
            if (expiringResult.getSeverity() == AiSeverity.CRITICAL) {
                overallSeverity = AiSeverity.CRITICAL;
            } else if (overallSeverity != AiSeverity.CRITICAL && expiringResult.getSeverity() == AiSeverity.WARNING) {
                overallSeverity = AiSeverity.WARNING;
            }

            insights.add(AiInsightResponse.builder()
                    .type("EXPIRING_SUBSCRIPTIONS")
                    .title("Subscriptions expiring soon")
                    .description(String.format("%d subscriptions expiring within 30 days, representing INR %s in recurring revenue at risk (earliest expiry: %s)",
                            expiringResult.getCount(), expiringResult.getPotentialRevenue(), expiringResult.getEarliestExpiryDate()))
                    .severity(expiringResult.getSeverity())
                    .affectedCustomers(expiringResult.getCustomers().size())
                    .affectedSubscriptions(expiringResult.getCount())
                    .potentialRevenue(expiringResult.getPotentialRevenue())
                    .potentialSavings(BigDecimal.ZERO)
                    .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                    .status(AiInsightStatus.NEW)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        // 3. Check Repeated Late Payments
        LatePaymentResult lateResult = latePaymentTool.analyze();
        if (lateResult.getLatePaymentCount() > 0) {
            if (lateResult.getSeverity() == AiSeverity.CRITICAL) {
                overallSeverity = AiSeverity.CRITICAL;
            } else if (overallSeverity != AiSeverity.CRITICAL && lateResult.getSeverity() == AiSeverity.WARNING) {
                overallSeverity = AiSeverity.WARNING;
            }

            insights.add(AiInsightResponse.builder()
                    .type("LATE_PAYMENTS")
                    .title("Repeated late payments identified")
                    .description(String.format("%d late payments recorded across %d customers totaling INR %s",
                            lateResult.getLatePaymentCount(), lateResult.getAffectedCustomerCount(), lateResult.getTotalAmountInvolved()))
                    .severity(lateResult.getSeverity())
                    .affectedCustomers(lateResult.getAffectedCustomerCount())
                    .potentialRevenue(lateResult.getTotalAmountInvolved())
                    .potentialSavings(BigDecimal.ZERO)
                    .recommendedAction(AiRecommendedAction.REVIEW_LATE_PAYMENTS)
                    .status(AiInsightStatus.NEW)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        // 4. Check Cost Optimization & Unused Capacity
        OptimizationSummaryResult optResult = optimizationSummaryTool.analyze();
        if (optResult.getPotentialMonthlySavings().compareTo(BigDecimal.ZERO) > 0) {
            if (overallSeverity != AiSeverity.CRITICAL && optResult.getSeverity() == AiSeverity.WARNING) {
                overallSeverity = AiSeverity.WARNING;
            }

            insights.add(AiInsightResponse.builder()
                    .type("OPTIMIZATION_SUMMARY")
                    .title("Cost optimization opportunities available")
                    .description(String.format("Potential monthly savings of INR %s (INR %s annually) from %d idle resource units across %d customers",
                            optResult.getPotentialMonthlySavings(), optResult.getPotentialAnnualSavings(), optResult.getUnusedCapacityTotal(), optResult.getAffectedCustomerCount()))
                    .severity(optResult.getSeverity())
                    .affectedCustomers(optResult.getAffectedCustomerCount())
                    .potentialRevenue(BigDecimal.ZERO)
                    .potentialSavings(optResult.getPotentialMonthlySavings())
                    .recommendedAction(AiRecommendedAction.REVIEW_OPTIMIZATION)
                    .status(AiInsightStatus.NEW)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        // If no risks detected:
        if (insights.isEmpty()) {
            insights.add(AiInsightResponse.builder()
                    .type("BUSINESS_OVERVIEW")
                    .title("Healthy business operations")
                    .description("No immediate overdue payments, expiring subscriptions, or excessive idle resources detected.")
                    .severity(AiSeverity.INFO)
                    .affectedCustomers(0)
                    .affectedSubscriptions(0)
                    .affectedInvoices(0)
                    .potentialRevenue(BigDecimal.ZERO)
                    .potentialSavings(BigDecimal.ZERO)
                    .recommendedAction(AiRecommendedAction.NO_ACTION)
                    .status(AiInsightStatus.NEW)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        return BusinessOverviewResult.builder()
                .title("Business Risk Overview")
                .severity(overallSeverity)
                .insights(insights)
                .build();
    }
}


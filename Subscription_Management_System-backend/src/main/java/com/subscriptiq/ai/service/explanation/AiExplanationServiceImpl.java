package com.subscriptiq.ai.service.explanation;

import com.subscriptiq.ai.dto.BusinessOverviewResult;
import com.subscriptiq.ai.dto.ExpiringSubscriptionResult;
import com.subscriptiq.ai.dto.LatePaymentResult;
import com.subscriptiq.ai.dto.OptimizationSummaryResult;
import com.subscriptiq.ai.dto.OverduePaymentResult;
import com.subscriptiq.ai.dto.PlanPerformanceResult;
import com.subscriptiq.ai.dto.RevenueSummaryResult;
import com.subscriptiq.ai.dto.SubscriptionSummaryResult;
import com.subscriptiq.ai.dto.UnusedResourceResult;
import com.subscriptiq.ai.enums.AiIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiExplanationServiceImpl implements AiExplanationService {

    private final LlmProvider llmProvider;

    @Override
    public String explain(AiIntent intent, Object structuredData) {
        if (llmProvider.isAvailable()) {
            try {
                String llmResult = llmProvider.generateExplanation(intent, structuredData);
                if (llmResult != null && !llmResult.trim().isEmpty()) {
                    return llmResult;
                }
            } catch (Exception e) {
                log.warn("LLM explanation generation failed, falling back to deterministic summary: {}", e.getMessage());
            }
        }

        return generateDeterministicExplanation(intent, structuredData);
    }

    private String generateDeterministicExplanation(AiIntent intent, Object data) {
        if (intent == null) {
            return "Unable to determine query intent.";
        }

        return switch (intent) {
            case BUSINESS_OVERVIEW -> {
                if (data instanceof BusinessOverviewResult r) {
                    yield String.format("Business risk overview indicates %s status with %d detected operational risk areas.",
                            r.getSeverity(), r.getInsights().size());
                }
                yield "Business overview assessment completed.";
            }
            case OVERDUE_PAYMENTS -> {
                if (data instanceof OverduePaymentResult r) {
                    if (r.getCount() == 0) {
                        yield "No overdue payments found. All issued invoices are currently within their payment terms.";
                    }
                    yield String.format("There are %d overdue invoices totaling INR %s. Oldest due date is %s (Invoice %s). Recommended action: Follow up with affected customers.",
                            r.getCount(), r.getTotalAmount(), r.getOldestDueDate(), r.getOldestInvoiceNumber());
                }
                yield "Overdue payments analysis completed.";
            }
            case EXPIRING_SUBSCRIPTIONS -> {
                if (data instanceof ExpiringSubscriptionResult r) {
                    if (r.getCount() == 0) {
                        yield "No subscriptions are expiring within the next 30 days.";
                    }
                    yield String.format("%d subscriptions are expiring within the next 30 days, representing INR %s in recurring revenue at risk. Earliest expiry: %s.",
                            r.getCount(), r.getPotentialRevenue(), r.getEarliestExpiryDate());
                }
                yield "Expiring subscriptions analysis completed.";
            }
            case LATE_PAYMENTS -> {
                if (data instanceof LatePaymentResult r) {
                    if (r.getLatePaymentCount() == 0) {
                        yield "No repeated late payments detected. Customer payment history is healthy.";
                    }
                    yield String.format("Identified %d late payments totaling INR %s across %d customers.",
                            r.getLatePaymentCount(), r.getTotalAmountInvolved(), r.getAffectedCustomerCount());
                }
                yield "Late payment analysis completed.";
            }
            case REVENUE_SUMMARY -> {
                if (data instanceof RevenueSummaryResult r) {
                    yield String.format("Total invoiced revenue stands at INR %s with INR %s collected (%s%% collection rate) and INR %s in outstanding balance.",
                            r.getTotalInvoiced(), r.getTotalCollected(), r.getCollectionRatePercentage(), r.getTotalOutstanding());
                }
                yield "Revenue summary calculation completed.";
            }
            case SUBSCRIPTION_SUMMARY -> {
                if (data instanceof SubscriptionSummaryResult r) {
                    yield String.format("Total of %d subscriptions: %d active, %d paused, and %d closed/expired, generating INR %s in estimated monthly recurring revenue.",
                            r.getTotalSubscriptions(), r.getActiveSubscriptions(), r.getPausedSubscriptions(), r.getClosedOrExpiredSubscriptions(), r.getMonthlyRecurringRevenue());
                }
                yield "Subscription summary completed.";
            }
            case UNUSED_RESOURCES -> {
                if (data instanceof UnusedResourceResult r) {
                    if (r.getTotalUnused() == 0) {
                        yield "All allocated resources are currently fully utilized. No idle capacity detected.";
                    }
                    yield String.format("Identified %d unused resource units across %d customers (%d allocated, %d used).",
                            r.getTotalUnused(), r.getAffectedCustomerCount(), r.getTotalAllocated(), r.getTotalUsed());
                }
                yield "Unused resource analysis completed.";
            }
            case OPTIMIZATION_SUMMARY -> {
                if (data instanceof OptimizationSummaryResult r) {
                    if (r.getPotentialMonthlySavings().compareTo(BigDecimal.ZERO) == 0) {
                        yield "Resource allocations are currently optimal. No immediate cost savings opportunities identified.";
                    }
                    yield String.format("Cost optimization analysis identified potential monthly savings of INR %s (INR %s annually) across %d idle units.",
                            r.getPotentialMonthlySavings(), r.getPotentialAnnualSavings(), r.getUnusedCapacityTotal());
                }
                yield "Optimization summary completed.";
            }
            case PLAN_PERFORMANCE -> {
                if (data instanceof PlanPerformanceResult r) {
                    yield String.format("Analyzed %d subscription plans. %s", r.getTotalPlans(), r.getNote());
                }
                yield "Plan performance analysis completed.";
            }
            case UNSUPPORTED_QUERY ->
                    "I am a business analytics assistant. I can only answer questions about business overview, overdue payments, expiring subscriptions, late payers, revenue, subscriptions, unused resources, cost savings, or plan performance. Arbitrary SQL queries or system actions are not supported.";
        };
    }
}


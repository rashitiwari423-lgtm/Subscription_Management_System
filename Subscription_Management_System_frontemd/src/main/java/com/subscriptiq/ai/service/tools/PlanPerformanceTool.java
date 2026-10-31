package com.subscriptiq.ai.service.tools;

import com.subscriptiq.ai.dto.PlanPerformanceResult;
import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.repository.PlanRepository;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanPerformanceTool {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    public PlanPerformanceResult analyze() {
        List<Plan> plans = planRepository.findAll();
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();

        List<PlanPerformanceResult.PlanStatDto> stats = new ArrayList<>();

        for (Plan plan : plans) {
            int active = 0;
            int pausedOrClosed = 0;
            BigDecimal planRevenue = BigDecimal.ZERO;

            for (Subscription sub : allSubscriptions) {
                if (sub.getPlan() != null && sub.getPlan().getId().equals(plan.getId())) {
                    if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
                        active++;
                        BigDecimal amt = sub.getTotalAmount() != null ? sub.getTotalAmount() : BigDecimal.ZERO;
                        planRevenue = planRevenue.add(amt);
                    } else if (sub.getStatus() == SubscriptionStatus.PAUSED || sub.getStatus() == SubscriptionStatus.CLOSED || sub.getStatus() == SubscriptionStatus.EXPIRED) {
                        pausedOrClosed++;
                    }
                }
            }

            stats.add(PlanPerformanceResult.PlanStatDto.builder()
                    .planId(plan.getId())
                    .planName(plan.getName())
                    .billingCycle(plan.getBillingCycle() != null ? plan.getBillingCycle().name() : "MONTHLY")
                    .price(plan.getPrice() != null ? plan.getPrice() : BigDecimal.ZERO)
                    .activeSubscriptions(active)
                    .pausedOrClosedSubscriptions(pausedOrClosed)
                    .monthlyRevenue(planRevenue.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        return PlanPerformanceResult.builder()
                .totalPlans(plans.size())
                .planStats(stats)
                .note("Predictive churn is reserved for future analytics phases.")
                .build();
    }
}


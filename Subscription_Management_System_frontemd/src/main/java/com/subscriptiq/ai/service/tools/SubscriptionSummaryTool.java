package com.subscriptiq.ai.service.tools;

import com.subscriptiq.ai.dto.SubscriptionSummaryResult;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionSummaryTool {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionSummaryResult analyze() {
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();

        int total = allSubscriptions.size();
        int active = 0;
        int paused = 0;
        int closedOrExpired = 0;
        BigDecimal mrr = BigDecimal.ZERO;

        for (Subscription sub : allSubscriptions) {
            if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
                active++;
                BigDecimal amount = sub.getTotalAmount() != null ? sub.getTotalAmount() : BigDecimal.ZERO;
                mrr = mrr.add(amount);
            } else if (sub.getStatus() == SubscriptionStatus.PAUSED) {
                paused++;
            } else if (sub.getStatus() == SubscriptionStatus.EXPIRED || sub.getStatus() == SubscriptionStatus.CLOSED) {
                closedOrExpired++;
            }
        }

        return SubscriptionSummaryResult.builder()
                .totalSubscriptions(total)
                .activeSubscriptions(active)
                .pausedSubscriptions(paused)
                .closedOrExpiredSubscriptions(closedOrExpired)
                .monthlyRecurringRevenue(mrr.setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}


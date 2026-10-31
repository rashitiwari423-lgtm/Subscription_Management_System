package com.subscriptiq.ai.service.tools;

import com.subscriptiq.ai.dto.CustomerSummaryDto;
import com.subscriptiq.ai.dto.ExpiringSubscriptionResult;
import com.subscriptiq.ai.enums.AiRecommendedAction;
import com.subscriptiq.ai.enums.AiSeverity;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiringSubscriptionTool {

    private final SubscriptionRepository subscriptionRepository;

    public ExpiringSubscriptionResult analyze() {
        LocalDate today = LocalDate.now();
        LocalDate cutoffDate = today.plusDays(30);

        List<Subscription> activeSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
        List<Subscription> expiring = new ArrayList<>();

        for (Subscription sub : activeSubscriptions) {
            LocalDate expiry = sub.getEndDate() != null ? sub.getEndDate() : sub.getNextBillingDate();
            if (expiry != null && !expiry.isBefore(today) && !expiry.isAfter(cutoffDate)) {
                expiring.add(sub);
            }
        }

        int count = expiring.size();
        BigDecimal potentialRevenue = BigDecimal.ZERO;
        LocalDate earliestExpiryDate = null;
        List<CustomerSummaryDto> customers = new ArrayList<>();

        if (!expiring.isEmpty()) {
            expiring.sort(Comparator.comparing(s -> s.getEndDate() != null ? s.getEndDate() : s.getNextBillingDate()));
            Subscription earliest = expiring.get(0);
            earliestExpiryDate = earliest.getEndDate() != null ? earliest.getEndDate() : earliest.getNextBillingDate();

            for (Subscription sub : expiring) {
                BigDecimal amount = sub.getTotalAmount() != null ? sub.getTotalAmount() : BigDecimal.ZERO;
                potentialRevenue = potentialRevenue.add(amount);

                LocalDate expiry = sub.getEndDate() != null ? sub.getEndDate() : sub.getNextBillingDate();
                long daysRemaining = ChronoUnit.DAYS.between(today, expiry);

                customers.add(CustomerSummaryDto.builder()
                        .customerId(sub.getCustomer() != null ? sub.getCustomer().getId() : null)
                        .companyName(sub.getCustomer() != null ? sub.getCustomer().getCompanyName() : "Unknown")
                        .email(sub.getCustomer() != null ? sub.getCustomer().getEmail() : null)
                        .amount(amount)
                        .detail(String.format("Subscription %s (expires in %d days)", sub.getSubscriptionNumber(), Math.max(0, daysRemaining)))
                        .build());
            }
        }

        AiSeverity severity;
        if (count == 0) {
            severity = AiSeverity.INFO;
        } else if (potentialRevenue.compareTo(new BigDecimal("50000.00")) >= 0 || count >= 5) {
            severity = AiSeverity.CRITICAL;
        } else {
            severity = AiSeverity.WARNING;
        }

        return ExpiringSubscriptionResult.builder()
                .count(count)
                .earliestExpiryDate(earliestExpiryDate)
                .potentialRevenue(potentialRevenue)
                .severity(severity)
                .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                .customers(customers)
                .build();
    }
}


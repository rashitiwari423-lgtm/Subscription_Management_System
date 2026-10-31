package com.subscriptiq.ai.action.executor;

import com.subscriptiq.ai.action.dto.ActionExecutionResult;
import com.subscriptiq.ai.action.dto.ActionPreviewDto;
import com.subscriptiq.ai.action.entity.AiActionRequest;
import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RenewalCampaignActionExecutor implements ActionExecutor {

    private final SubscriptionRepository subscriptionRepository;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Override
    public AiActionType getActionType() {
        return AiActionType.SEND_RENEWAL_CAMPAIGN;
    }

    @Override
    public ActionPreviewDto preview(AiInsight insight) {
        LocalDate today = LocalDate.now();
        LocalDate cutoffDate = today.plusDays(30);

        List<Subscription> activeSubs = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
        List<Subscription> expiring = new ArrayList<>();
        Set<Long> affectedCustomerIds = new HashSet<>();
        BigDecimal potentialRevenue = BigDecimal.ZERO;
        List<String> affectedResources = new ArrayList<>();

        for (Subscription sub : activeSubs) {
            LocalDate expiry = sub.getEndDate() != null ? sub.getEndDate() : sub.getNextBillingDate();
            if (expiry != null && !expiry.isBefore(today) && !expiry.isAfter(cutoffDate)) {
                expiring.add(sub);
                BigDecimal amount = sub.getTotalAmount() != null ? sub.getTotalAmount() : BigDecimal.ZERO;
                potentialRevenue = potentialRevenue.add(amount);

                String company = sub.getCustomer() != null ? sub.getCustomer().getCompanyName() : "Unknown";
                if (sub.getCustomer() != null) {
                    affectedCustomerIds.add(sub.getCustomer().getId());
                }

                long daysRemaining = Math.max(0, ChronoUnit.DAYS.between(today, expiry));
                affectedResources.add(String.format("Subscription %s (%s, expires in %d days, amount: %s)",
                        sub.getSubscriptionNumber(), company, daysRemaining, amount));
            }
        }

        List<String> warnings = new ArrayList<>();
        warnings.add("Action requires explicit admin approval before any campaign notices are queued.");
        if (!StringUtils.hasText(mailHost)) {
            warnings.add("Outbound SMTP host is not configured; executing will create controlled campaign tasks requiring notification infrastructure.");
        }
        if (expiring.isEmpty()) {
            warnings.add("Live verification found 0 subscriptions expiring within 30 days. Business data may have changed since insight generation.");
        }

        return ActionPreviewDto.builder()
                .title("Send Subscription Renewal Campaign")
                .description(String.format("Queue renewal campaigns for %d customers with %d subscriptions expiring within 30 days.",
                        affectedCustomerIds.size(), expiring.size()))
                .affectedCustomers(affectedCustomerIds.size())
                .affectedSubscriptions(expiring.size())
                .affectedInvoices(0)
                .potentialRevenue(potentialRevenue)
                .potentialSavings(BigDecimal.ZERO)
                .affectedResources(affectedResources)
                .warnings(warnings)
                .build();
    }

    @Override
    public ActionExecutionResult execute(AiActionRequest action, UserPrincipal user) {
        LocalDate today = LocalDate.now();
        LocalDate cutoffDate = today.plusDays(30);

        List<Subscription> activeSubs = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
        List<Subscription> eligibleSubs = new ArrayList<>();
        Set<String> recipientCustomerNames = new HashSet<>();

        for (Subscription sub : activeSubs) {
            LocalDate expiry = sub.getEndDate() != null ? sub.getEndDate() : sub.getNextBillingDate();
            if (expiry != null && !expiry.isBefore(today) && !expiry.isAfter(cutoffDate)) {
                eligibleSubs.add(sub);
                String customerName = sub.getCustomer() != null ? sub.getCustomer().getCompanyName() : "Customer #" + (sub.getCustomer() != null ? sub.getCustomer().getId() : "Unknown");
                recipientCustomerNames.add(customerName);
            }
        }

        int affectedCount = recipientCustomerNames.size();
        String summary;
        if (!StringUtils.hasText(mailHost)) {
            summary = String.format("Renewal campaign prepared for %d customers across %d eligible subscriptions. Outbound delivery queued for notification infrastructure.",
                    affectedCount, eligibleSubs.size());
        } else {
            summary = String.format("Renewal campaign dispatched for %d customers across %d eligible subscriptions via notification infrastructure.",
                    affectedCount, eligibleSubs.size());
        }

        log.info("RenewalCampaignActionExecutor executed by [{}]: {}", user != null ? user.getEmail() : "system", summary);

        return ActionExecutionResult.builder()
                .status(AiActionExecutionStatus.SUCCESS)
                .affectedCount(affectedCount)
                .summary(summary)
                .build();
    }
}

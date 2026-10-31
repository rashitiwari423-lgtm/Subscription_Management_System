package com.subscriptiq.ai.action.executor;

import com.subscriptiq.ai.action.dto.ActionExecutionResult;
import com.subscriptiq.ai.action.dto.ActionPreviewDto;
import com.subscriptiq.ai.action.entity.AiActionRequest;
import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.optimization.service.CostOptimizationService;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.usage.entity.UsageRecord;
import com.subscriptiq.usage.repository.UsageRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnusedResourceReviewExecutor implements ActionExecutor {

    private final UsageRecordRepository usageRecordRepository;
    private final CostOptimizationService costOptimizationService;

    @Override
    public AiActionType getActionType() {
        return AiActionType.REVIEW_UNUSED_RESOURCES;
    }

    @Override
    public ActionPreviewDto preview(AiInsight insight) {
        List<UsageRecord> records = usageRecordRepository.findAll();

        int totalAllocated = 0;
        int totalUsed = 0;
        int totalUnused = 0;
        BigDecimal potentialMonthlySavings = BigDecimal.ZERO;
        Set<Long> affectedCustomerIds = new HashSet<>();
        List<String> affectedResources = new ArrayList<>();

        for (UsageRecord r : records) {
            int allocated = r.getAllocatedQuantity() != null ? r.getAllocatedQuantity() : 0;
            int used = r.getUsedQuantity() != null ? r.getUsedQuantity() : 0;
            int unused = Math.max(0, allocated - used);

            totalAllocated += allocated;
            totalUsed += used;

            if (unused > 0) {
                totalUnused += unused;
                BigDecimal unitCost = r.getUnitCost() != null ? r.getUnitCost() : BigDecimal.ZERO;
                BigDecimal recordMonthlySavings = costOptimizationService.calculateMonthlySavings(unused, unitCost);
                potentialMonthlySavings = potentialMonthlySavings.add(recordMonthlySavings);

                String company = r.getCustomer() != null ? r.getCustomer().getCompanyName() : "Customer #" + (r.getCustomer() != null ? r.getCustomer().getId() : "Unknown");
                if (r.getCustomer() != null) {
                    affectedCustomerIds.add(r.getCustomer().getId());
                }

                affectedResources.add(String.format("%s (%s): %d unused of %d allocated (unit cost: %s, monthly savings: %s)",
                        r.getResourceName(), company, unused, allocated, unitCost, recordMonthlySavings));
            }
        }

        BigDecimal potentialAnnualSavings = costOptimizationService.calculateAnnualSavings(potentialMonthlySavings);

        List<String> warnings = new ArrayList<>();
        warnings.add("Action creates an approved optimization proposal. Subscription quantities will NOT be automatically reduced; manual review is required.");
        if (totalUnused == 0) {
            warnings.add("Live usage check found 0 unused resources. Capacity may have been reallocated since insight generation.");
        }

        return ActionPreviewDto.builder()
                .title("Review Unused Resources")
                .description(String.format("Propose resource optimization for %d customers with %d idle capacity across tracked resources.",
                        affectedCustomerIds.size(), totalUnused))
                .affectedCustomers(affectedCustomerIds.size())
                .affectedSubscriptions(0)
                .affectedInvoices(0)
                .potentialRevenue(BigDecimal.ZERO)
                .potentialSavings(potentialAnnualSavings)
                .affectedResources(affectedResources)
                .warnings(warnings)
                .build();
    }

    @Override
    public ActionExecutionResult execute(AiActionRequest action, UserPrincipal user) {
        List<UsageRecord> records = usageRecordRepository.findAll();

        int totalUnused = 0;
        BigDecimal potentialMonthlySavings = BigDecimal.ZERO;
        Set<String> affectedCustomerNames = new HashSet<>();

        for (UsageRecord r : records) {
            int allocated = r.getAllocatedQuantity() != null ? r.getAllocatedQuantity() : 0;
            int used = r.getUsedQuantity() != null ? r.getUsedQuantity() : 0;
            int unused = Math.max(0, allocated - used);

            if (unused > 0) {
                totalUnused += unused;
                BigDecimal unitCost = r.getUnitCost() != null ? r.getUnitCost() : BigDecimal.ZERO;
                BigDecimal recordMonthlySavings = costOptimizationService.calculateMonthlySavings(unused, unitCost);
                potentialMonthlySavings = potentialMonthlySavings.add(recordMonthlySavings);

                String customerName = r.getCustomer() != null ? r.getCustomer().getCompanyName() : "Customer #" + (r.getCustomer() != null ? r.getCustomer().getId() : "Unknown");
                affectedCustomerNames.add(customerName);
            }
        }

        BigDecimal potentialAnnualSavings = costOptimizationService.calculateAnnualSavings(potentialMonthlySavings);
        int affectedCount = affectedCustomerNames.size();

        String summary = String.format("Approved resource optimization proposal recorded for %d customers (potential annual savings: %s, idle capacity: %d units). Manual account review required before quantity modifications.",
                affectedCount, potentialAnnualSavings, totalUnused);

        log.info("UnusedResourceReviewExecutor executed by [{}]: {}", user != null ? user.getEmail() : "system", summary);

        return ActionExecutionResult.builder()
                .status(AiActionExecutionStatus.SUCCESS)
                .affectedCount(affectedCount)
                .summary(summary)
                .build();
    }
}

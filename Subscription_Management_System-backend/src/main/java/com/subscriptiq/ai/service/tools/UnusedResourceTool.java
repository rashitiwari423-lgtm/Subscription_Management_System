package com.subscriptiq.ai.service.tools;

import com.subscriptiq.ai.dto.CustomerSummaryDto;
import com.subscriptiq.ai.dto.UnusedResourceResult;
import com.subscriptiq.ai.enums.AiRecommendedAction;
import com.subscriptiq.ai.enums.AiSeverity;
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
public class UnusedResourceTool {

    private final UsageRecordRepository usageRecordRepository;

    public UnusedResourceResult analyze() {
        List<UsageRecord> records = usageRecordRepository.findAll();

        int totalAllocated = 0;
        int totalUsed = 0;
        int totalUnused = 0;
        int unusedRecordCount = 0;
        Set<Long> affectedCustomerIds = new HashSet<>();
        List<CustomerSummaryDto> details = new ArrayList<>();

        for (UsageRecord r : records) {
            int allocated = r.getAllocatedQuantity() != null ? r.getAllocatedQuantity() : 0;
            int used = r.getUsedQuantity() != null ? r.getUsedQuantity() : 0;
            int unused = Math.max(0, allocated - used);

            totalAllocated += allocated;
            totalUsed += used;

            if (unused > 0) {
                unusedRecordCount++;
                totalUnused += unused;
                if (r.getCustomer() != null) {
                    affectedCustomerIds.add(r.getCustomer().getId());
                    BigDecimal cost = r.getUnitCost() != null ? r.getUnitCost().multiply(BigDecimal.valueOf(unused)) : BigDecimal.ZERO;
                    details.add(CustomerSummaryDto.builder()
                            .customerId(r.getCustomer().getId())
                            .companyName(r.getCustomer().getCompanyName())
                            .email(r.getCustomer().getEmail())
                            .amount(cost)
                            .detail(String.format("%s: %d unused of %d allocated (%s)",
                                    r.getResourceName(), unused, allocated, r.getResourceType()))
                            .build());
                }
            }
        }

        AiSeverity severity = totalUnused > 0 ? (totalUnused >= 50 ? AiSeverity.CRITICAL : AiSeverity.WARNING) : AiSeverity.INFO;

        return UnusedResourceResult.builder()
                .unusedRecordCount(unusedRecordCount)
                .totalAllocated(totalAllocated)
                .totalUsed(totalUsed)
                .totalUnused(totalUnused)
                .affectedCustomerCount(affectedCustomerIds.size())
                .severity(severity)
                .recommendedAction(AiRecommendedAction.REVIEW_UNUSED_RESOURCES)
                .resources(details)
                .build();
    }
}


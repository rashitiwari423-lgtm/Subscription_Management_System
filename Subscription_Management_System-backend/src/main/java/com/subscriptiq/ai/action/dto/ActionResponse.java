package com.subscriptiq.ai.action.dto;

import com.subscriptiq.ai.action.enums.AiActionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionResponse {

    private Long id;
    private Long insightId;
    private AiActionType actionType;
    private AiActionStatus status;
    private String title;
    private String description;
    private Integer affectedCustomers;
    private Integer affectedSubscriptions;
    private Integer affectedInvoices;
    private BigDecimal potentialRevenue;
    private BigDecimal potentialSavings;
    private List<String> affectedResources;
    private List<String> warnings;
    private boolean requiresApproval;
    private String idempotencyKey;
    private String requestedBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectedBy;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


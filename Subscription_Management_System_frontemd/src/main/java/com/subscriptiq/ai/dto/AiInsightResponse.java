package com.subscriptiq.ai.dto;

import com.subscriptiq.ai.enums.AiInsightStatus;
import com.subscriptiq.ai.enums.AiRecommendedAction;
import com.subscriptiq.ai.enums.AiSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representation of a structured AI insight")
public class AiInsightResponse {

    @Schema(description = "Insight unique ID", example = "1")
    private Long id;

    @Schema(description = "Insight category / intent", example = "EXPIRING_SUBSCRIPTIONS")
    private String type;

    @Schema(description = "Insight title", example = "Subscriptions expiring soon")
    private String title;

    @Schema(description = "Detailed factual description", example = "4 subscriptions are expiring within the next 30 days, representing INR 86,000.00 at risk.")
    private String description;

    @Schema(description = "Severity level", example = "CRITICAL")
    private AiSeverity severity;

    @Schema(description = "Count of affected customers", example = "14")
    private Integer affectedCustomers;

    @Schema(description = "Count of affected subscriptions", example = "14")
    private Integer affectedSubscriptions;

    @Schema(description = "Count of affected invoices", example = "0")
    private Integer affectedInvoices;

    @Schema(description = "Potential revenue involved", example = "86000.00")
    private BigDecimal potentialRevenue;

    @Schema(description = "Potential cost savings involved", example = "0.00")
    private BigDecimal potentialSavings;

    @Schema(description = "Recommended advisory action", example = "SEND_RENEWAL_CAMPAIGN")
    private AiRecommendedAction recommendedAction;

    @Schema(description = "Lifecycle status", example = "NEW")
    private AiInsightStatus status;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}


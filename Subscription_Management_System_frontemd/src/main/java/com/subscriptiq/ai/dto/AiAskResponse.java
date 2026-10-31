package com.subscriptiq.ai.dto;

import com.subscriptiq.ai.enums.AiIntent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Structured response from the AI Subscription Command Center")
public class AiAskResponse {

    @Schema(description = "Original input question", example = "Mere business mein abhi kya problem hai?")
    private String question;

    @Schema(description = "Classified controlled business intent", example = "BUSINESS_OVERVIEW")
    private AiIntent detectedIntent;

    @Schema(description = "Natural language explanation generated from deterministic facts",
            example = "Business overview identified 2 critical areas: 3 overdue invoices totaling INR 45,000.00 and 18 unused software licenses with potential annual savings of INR 432,000.00.")
    private String summary;

    @Schema(description = "Persisted structured insights supported by actual database records")
    @Builder.Default
    private List<AiInsightResponse> insights = new ArrayList<>();

    @Schema(description = "Advisory operational recommendations")
    @Builder.Default
    private List<String> recommendations = new ArrayList<>();

    @Schema(description = "Response generation timestamp")
    private LocalDateTime generatedAt;
}


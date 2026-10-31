package com.subscriptiq.ai.dto;

import com.subscriptiq.ai.enums.AiSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Structured business risk overview aggregating key operational operational risks")
public class BusinessOverviewResult {

    @Schema(description = "Overall overview title", example = "Business Risk Overview")
    @Builder.Default
    private String title = "Business Risk Overview";

    @Schema(description = "Overall highest severity across detected risk categories", example = "CRITICAL")
    private AiSeverity severity;

    @Schema(description = "Structured detected risk insights supported by persisted data")
    @Builder.Default
    private List<AiInsightResponse> insights = new ArrayList<>();
}


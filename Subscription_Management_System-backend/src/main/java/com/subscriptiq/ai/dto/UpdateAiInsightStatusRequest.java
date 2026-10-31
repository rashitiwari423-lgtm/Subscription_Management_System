package com.subscriptiq.ai.dto;

import com.subscriptiq.ai.enums.AiInsightStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to update the lifecycle status of an AI insight")
public class UpdateAiInsightStatusRequest {

    @NotNull(message = "Status cannot be null")
    @Schema(description = "New status for the insight", example = "RESOLVED", requiredMode = Schema.RequiredMode.REQUIRED)
    private AiInsightStatus status;
}


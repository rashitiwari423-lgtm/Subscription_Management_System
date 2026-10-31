package com.subscriptiq.ai.action.dto;

import com.subscriptiq.ai.action.enums.AiActionType;
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
public class ActionPreviewRequest {

    @NotNull(message = "insightId is required")
    private Long insightId;

    @NotNull(message = "actionType is required")
    private AiActionType actionType;

    private String idempotencyKey;
}


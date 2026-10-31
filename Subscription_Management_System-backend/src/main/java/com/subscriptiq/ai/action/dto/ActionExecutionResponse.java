package com.subscriptiq.ai.action.dto;

import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionExecutionResponse {

    private Long executionId;
    private Long actionId;
    private Long insightId;
    private AiActionType actionType;
    private AiActionExecutionStatus status;
    private Integer affectedCount;
    private String resultSummary;
    private String errorCode;
    private String executedBy;
    private LocalDateTime executedAt;
    private boolean previouslyExecuted;
}


package com.subscriptiq.ai.action.dto;

import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
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
public class ActionExecutionResult {

    private AiActionExecutionStatus status;
    private Integer affectedCount;
    private String summary;
    private String errorCode;
}


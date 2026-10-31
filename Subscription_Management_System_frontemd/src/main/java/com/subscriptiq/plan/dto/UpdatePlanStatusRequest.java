package com.subscriptiq.plan.dto;

import com.subscriptiq.plan.enums.PlanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlanStatusRequest {

    @NotNull(message = "Status is required (ACTIVE or INACTIVE)")
    private PlanStatus status;
}


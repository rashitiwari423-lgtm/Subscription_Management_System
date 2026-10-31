package com.subscriptiq.discount.dto;

import com.subscriptiq.discount.entity.Discount;
import com.subscriptiq.discount.enums.DiscountStatus;
import com.subscriptiq.discount.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountResponse {

    private Long id;
    private String name;
    private DiscountType type;
    private BigDecimal value;
    private DiscountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DiscountResponse fromEntity(Discount discount) {
        if (discount == null) {
            return null;
        }
        return DiscountResponse.builder()
                .id(discount.getId())
                .name(discount.getName())
                .type(discount.getType())
                .value(discount.getValue())
                .status(discount.getStatus())
                .createdAt(discount.getCreatedAt())
                .updatedAt(discount.getUpdatedAt())
                .build();
    }
}


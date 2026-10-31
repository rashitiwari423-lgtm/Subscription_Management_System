package com.subscriptiq.tax.dto;

import com.subscriptiq.tax.entity.Tax;
import com.subscriptiq.tax.enums.TaxStatus;
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
public class TaxResponse {

    private Long id;
    private String name;
    private BigDecimal percentage;
    private TaxStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaxResponse fromEntity(Tax tax) {
        if (tax == null) {
            return null;
        }
        return TaxResponse.builder()
                .id(tax.getId())
                .name(tax.getName())
                .percentage(tax.getPercentage())
                .status(tax.getStatus())
                .createdAt(tax.getCreatedAt())
                .updatedAt(tax.getUpdatedAt())
                .build();
    }
}


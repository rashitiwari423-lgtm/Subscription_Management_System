package com.subscriptiq.invoice.dto;

import com.subscriptiq.invoice.entity.InvoiceItem;
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
public class InvoiceItemResponse {

    private Long id;
    private Long planId;
    private String planName;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InvoiceItemResponse fromEntity(InvoiceItem item) {
        if (item == null) {
            return null;
        }
        return InvoiceItemResponse.builder()
                .id(item.getId())
                .planId(item.getPlan() != null ? item.getPlan().getId() : null)
                .planName(item.getPlan() != null ? item.getPlan().getName() : null)
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())
                .taxAmount(item.getTaxAmount())
                .totalAmount(item.getTotalAmount())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}


package com.subscriptiq.product.dto;

import com.subscriptiq.product.entity.ProductVariant;
import com.subscriptiq.product.enums.VariantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {

    private Long id;
    private Long productId;
    private String name;
    private String sku;
    private String description;
    private VariantStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductVariantResponse fromEntity(ProductVariant variant) {
        if (variant == null) {
            return null;
        }
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .productId(variant.getProductId())
                .name(variant.getName())
                .sku(variant.getSku())
                .description(variant.getDescription())
                .status(variant.getStatus())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }
}


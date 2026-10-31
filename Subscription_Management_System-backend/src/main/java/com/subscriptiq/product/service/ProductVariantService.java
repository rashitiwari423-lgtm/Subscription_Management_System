package com.subscriptiq.product.service;

import com.subscriptiq.product.dto.CreateVariantRequest;
import com.subscriptiq.product.dto.ProductVariantResponse;
import com.subscriptiq.product.dto.UpdateVariantRequest;
import com.subscriptiq.product.enums.VariantStatus;

import java.util.List;

public interface ProductVariantService {

    ProductVariantResponse createVariant(Long productId, CreateVariantRequest request);

    List<ProductVariantResponse> getVariantsByProductId(Long productId, boolean activeOnly);

    ProductVariantResponse getVariantById(Long id, boolean activeOnly);

    ProductVariantResponse updateVariant(Long id, UpdateVariantRequest request);

    ProductVariantResponse updateVariantStatus(Long id, VariantStatus status);
}


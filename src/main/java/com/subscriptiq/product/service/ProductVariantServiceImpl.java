package com.subscriptiq.product.service;

import com.subscriptiq.common.exception.DuplicateResourceException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.product.dto.CreateVariantRequest;
import com.subscriptiq.product.dto.ProductVariantResponse;
import com.subscriptiq.product.dto.UpdateVariantRequest;
import com.subscriptiq.product.entity.Product;
import com.subscriptiq.product.entity.ProductVariant;
import com.subscriptiq.product.enums.VariantStatus;
import com.subscriptiq.product.repository.ProductRepository;
import com.subscriptiq.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    @Override
    @Transactional
    public ProductVariantResponse createVariant(Long productId, CreateVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        String normalizedSku = request.getSku().trim().toUpperCase();

        if (variantRepository.existsBySku(normalizedSku)) {
            throw new DuplicateResourceException("Product variant with SKU '" + normalizedSku + "' already exists");
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .name(request.getName().trim())
                .sku(normalizedSku)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .status(VariantStatus.ACTIVE)
                .build();

        ProductVariant saved = variantRepository.save(variant);
        log.info("Created variant [{}] with SKU [{}] for product id [{}]", saved.getName(), saved.getSku(), productId);

        return ProductVariantResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getVariantsByProductId(Long productId, boolean activeOnly) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        List<ProductVariant> variants = activeOnly
                ? variantRepository.findByProductIdAndStatus(productId, VariantStatus.ACTIVE)
                : variantRepository.findByProductId(productId);

        return variants.stream()
                .map(ProductVariantResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantResponse getVariantById(Long id, boolean activeOnly) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", id));

        if (activeOnly && variant.getStatus() != VariantStatus.ACTIVE) {
            throw new ResourceNotFoundException("ProductVariant", "id", id);
        }

        return ProductVariantResponse.fromEntity(variant);
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariant(Long id, UpdateVariantRequest request) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", id));

        String normalizedSku = request.getSku().trim().toUpperCase();
        if (variantRepository.existsBySkuAndIdNot(normalizedSku, id)) {
            throw new DuplicateResourceException("Product variant with SKU '" + normalizedSku + "' already exists");
        }

        variant.setName(request.getName().trim());
        variant.setSku(normalizedSku);
        variant.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        ProductVariant updated = variantRepository.save(variant);
        log.info("Updated variant [{}] with SKU [{}]", updated.getName(), updated.getSku());

        return ProductVariantResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariantStatus(Long id, VariantStatus status) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", id));

        variant.setStatus(status);
        ProductVariant updated = variantRepository.save(variant);
        log.info("Updated variant [{}] status to [{}]", updated.getId(), status);

        return ProductVariantResponse.fromEntity(updated);
    }
}


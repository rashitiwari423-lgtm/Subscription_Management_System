package com.subscriptiq.product.service;

import com.subscriptiq.common.exception.DuplicateResourceException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.product.dto.CreateVariantRequest;
import com.subscriptiq.product.dto.ProductVariantResponse;
import com.subscriptiq.product.dto.UpdateVariantRequest;
import com.subscriptiq.product.entity.Product;
import com.subscriptiq.product.entity.ProductVariant;
import com.subscriptiq.product.enums.ProductStatus;
import com.subscriptiq.product.enums.VariantStatus;
import com.subscriptiq.product.repository.ProductRepository;
import com.subscriptiq.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductVariantServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @InjectMocks
    private ProductVariantServiceImpl variantService;

    @Test
    @DisplayName("Should create variant under valid product")
    void testCreateVariant_Success() {
        Product product = Product.builder()
                .id(1L)
                .name("CRM Pro")
                .status(ProductStatus.ACTIVE)
                .build();

        CreateVariantRequest request = CreateVariantRequest.builder()
                .name("Enterprise Edition")
                .sku("CRM-ENT-001")
                .description("Enterprise grade CRM")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(variantRepository.existsBySku("CRM-ENT-001")).thenReturn(false);

        ProductVariant saved = ProductVariant.builder()
                .id(10L)
                .product(product)
                .name("Enterprise Edition")
                .sku("CRM-ENT-001")
                .status(VariantStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(variantRepository.save(any(ProductVariant.class))).thenReturn(saved);

        ProductVariantResponse response = variantService.createVariant(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getSku()).isEqualTo("CRM-ENT-001");
    }

    @Test
    @DisplayName("Should reject variant creation if product does not exist")
    void testCreateVariant_NonExistentProduct() {
        CreateVariantRequest request = CreateVariantRequest.builder()
                .name("Basic Edition")
                .sku("CRM-BAS-001")
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> variantService.createVariant(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");
    }

    @Test
    @DisplayName("Should reject variant creation if SKU already exists")
    void testCreateVariant_DuplicateSku() {
        Product product = Product.builder().id(1L).name("CRM").build();
        CreateVariantRequest request = CreateVariantRequest.builder()
                .name("Pro Edition")
                .sku("CRM-PRO-001")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(variantRepository.existsBySku("CRM-PRO-001")).thenReturn(true);

        assertThatThrownBy(() -> variantService.createVariant(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CRM-PRO-001");
    }

    @Test
    @DisplayName("Should update variant successfully")
    void testUpdateVariant_Success() {
        ProductVariant existing = ProductVariant.builder()
                .id(5L)
                .name("Old Variant")
                .sku("OLD-SKU-01")
                .status(VariantStatus.ACTIVE)
                .build();

        UpdateVariantRequest request = UpdateVariantRequest.builder()
                .name("Updated Variant")
                .sku("NEW-SKU-01")
                .description("Updated desc")
                .build();

        when(variantRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(variantRepository.existsBySkuAndIdNot("NEW-SKU-01", 5L)).thenReturn(false);
        when(variantRepository.save(any(ProductVariant.class))).thenAnswer(i -> i.getArgument(0));

        ProductVariantResponse response = variantService.updateVariant(5L, request);

        assertThat(response.getName()).isEqualTo("Updated Variant");
        assertThat(response.getSku()).isEqualTo("NEW-SKU-01");
    }

    @Test
    @DisplayName("Should deactivate variant status")
    void testUpdateVariantStatus_Deactivate() {
        ProductVariant existing = ProductVariant.builder()
                .id(5L)
                .name("Active Variant")
                .status(VariantStatus.ACTIVE)
                .build();

        when(variantRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(variantRepository.save(any(ProductVariant.class))).thenAnswer(i -> i.getArgument(0));

        ProductVariantResponse response = variantService.updateVariantStatus(5L, VariantStatus.INACTIVE);

        assertThat(response.getStatus()).isEqualTo(VariantStatus.INACTIVE);
        verify(variantRepository).save(existing);
    }
}


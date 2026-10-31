package com.subscriptiq.product.service;

import com.subscriptiq.common.exception.DuplicateResourceException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.product.dto.CreateProductRequest;
import com.subscriptiq.product.dto.ProductResponse;
import com.subscriptiq.product.dto.UpdateProductRequest;
import com.subscriptiq.product.entity.Product;
import com.subscriptiq.product.enums.ProductStatus;
import com.subscriptiq.product.repository.ProductRepository;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct_Success() {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Cloud Billing")
                .description("Cloud billing engine")
                .category("SaaS")
                .build();

        when(productRepository.existsByName("Cloud Billing")).thenReturn(false);

        Product saved = Product.builder()
                .id(1L)
                .name("Cloud Billing")
                .description("Cloud billing engine")
                .category("SaaS")
                .status(ProductStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.createProduct(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Cloud Billing");
        assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should reject product creation with duplicate name")
    void testCreateProduct_DuplicateName() {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Cloud Billing")
                .build();

        when(productRepository.existsByName("Cloud Billing")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Cloud Billing");
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct_Success() {
        Product existing = Product.builder()
                .id(1L)
                .name("Old Product")
                .description("Old Desc")
                .status(ProductStatus.ACTIVE)
                .build();

        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("New Product")
                .description("New Desc")
                .category("Finance")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsByNameAndIdNot("New Product", 1L)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductResponse response = productService.updateProduct(1L, request);

        assertThat(response.getName()).isEqualTo("New Product");
        assertThat(response.getCategory()).isEqualTo("Finance");
    }

    @Test
    @DisplayName("Should deactivate product status")
    void testUpdateProductStatus_Deactivate() {
        Product existing = Product.builder()
                .id(1L)
                .name("Active Product")
                .status(ProductStatus.ACTIVE)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductResponse response = productService.updateProductStatus(1L, ProductStatus.INACTIVE);

        assertThat(response.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        verify(productRepository).save(existing);
    }

    @Test
    @DisplayName("Customer requesting inactive product should receive ResourceNotFoundException")
    void testGetProductById_ActiveOnly_InactiveProduct() {
        Product inactiveProduct = Product.builder()
                .id(2L)
                .name("Legacy System")
                .status(ProductStatus.INACTIVE)
                .build();

        when(productRepository.findById(2L)).thenReturn(Optional.of(inactiveProduct));

        assertThatThrownBy(() -> productService.getProductById(2L, true))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}


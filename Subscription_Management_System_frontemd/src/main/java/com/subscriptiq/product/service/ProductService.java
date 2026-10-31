package com.subscriptiq.product.service;

import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.product.dto.CreateProductRequest;
import com.subscriptiq.product.dto.ProductResponse;
import com.subscriptiq.product.dto.UpdateProductRequest;
import com.subscriptiq.product.enums.ProductStatus;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    PagedResponse<ProductResponse> getProducts(String search, String category, ProductStatus status, Pageable pageable);

    ProductResponse getProductById(Long id, boolean activeOnly);

    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    ProductResponse updateProductStatus(Long id, ProductStatus status);
}


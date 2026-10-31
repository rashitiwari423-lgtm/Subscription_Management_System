package com.subscriptiq.product.service;

import com.subscriptiq.common.exception.DuplicateResourceException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.product.dto.CreateProductRequest;
import com.subscriptiq.product.dto.ProductResponse;
import com.subscriptiq.product.dto.UpdateProductRequest;
import com.subscriptiq.product.entity.Product;
import com.subscriptiq.product.enums.ProductStatus;
import com.subscriptiq.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        String trimmedName = request.getName().trim();

        if (productRepository.existsByName(trimmedName)) {
            throw new DuplicateResourceException("Product with name '" + trimmedName + "' already exists");
        }

        Product product = Product.builder()
                .name(trimmedName)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .category(request.getCategory() != null ? request.getCategory().trim() : null)
                .status(ProductStatus.ACTIVE)
                .build();

        Product saved = productRepository.save(product);
        log.info("Created product [{}] with id [{}]", saved.getName(), saved.getId());

        return ProductResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getProducts(String search, String category, ProductStatus status, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(search)) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            Specification<Product> searchSpec = (root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("category")), pattern)
            );
            spec = spec.and(searchSpec);
        }

        if (StringUtils.hasText(category)) {
            Specification<Product> catSpec = (root, query, cb) ->
                    cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase());
            spec = spec.and(catSpec);
        }

        if (status != null) {
            Specification<Product> statusSpec = (root, query, cb) -> cb.equal(root.get("status"), status);
            spec = spec.and(statusSpec);
        }

        Page<ProductResponse> page = productRepository.findAll(spec, pageable)
                .map(ProductResponse::fromEntity);

        return PagedResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id, boolean activeOnly) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (activeOnly && product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        return ProductResponse.fromEntity(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        String trimmedName = request.getName().trim();
        if (productRepository.existsByNameAndIdNot(trimmedName, id)) {
            throw new DuplicateResourceException("Product with name '" + trimmedName + "' already exists");
        }

        product.setName(trimmedName);
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        product.setCategory(request.getCategory() != null ? request.getCategory().trim() : null);

        Product updated = productRepository.save(product);
        log.info("Updated product [{}] with id [{}]", updated.getName(), updated.getId());

        return ProductResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public ProductResponse updateProductStatus(Long id, ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setStatus(status);
        Product updated = productRepository.save(product);
        log.info("Updated product [{}] status to [{}]", updated.getId(), status);

        return ProductResponse.fromEntity(updated);
    }
}


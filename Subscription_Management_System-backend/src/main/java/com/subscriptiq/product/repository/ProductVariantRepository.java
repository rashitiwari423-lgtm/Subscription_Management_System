package com.subscriptiq.product.repository;

import com.subscriptiq.product.entity.ProductVariant;
import com.subscriptiq.product.enums.VariantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long>, JpaSpecificationExecutor<ProductVariant> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId")
    List<ProductVariant> findByProductId(@Param("productId") Long productId);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.status = :status")
    List<ProductVariant> findByProductIdAndStatus(@Param("productId") Long productId, @Param("status") VariantStatus status);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId")
    Page<ProductVariant> findByProductId(@Param("productId") Long productId, Pageable pageable);

    Optional<ProductVariant> findByIdAndStatus(Long id, VariantStatus status);
}

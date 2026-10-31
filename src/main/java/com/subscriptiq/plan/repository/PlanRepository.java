package com.subscriptiq.plan.repository;

import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.enums.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long>, JpaSpecificationExecutor<Plan> {

    @Query("SELECT p FROM Plan p WHERE p.productVariant.id = :productVariantId")
    List<Plan> findByProductVariantId(@Param("productVariantId") Long productVariantId);

    @Query("SELECT p FROM Plan p WHERE p.productVariant.id = :productVariantId AND p.status = :status")
    List<Plan> findByProductVariantIdAndStatus(@Param("productVariantId") Long productVariantId, @Param("status") PlanStatus status);

    Optional<Plan> findByIdAndStatus(Long id, PlanStatus status);
}

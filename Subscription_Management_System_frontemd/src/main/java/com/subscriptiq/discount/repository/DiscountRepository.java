package com.subscriptiq.discount.repository;

import com.subscriptiq.discount.entity.Discount;
import com.subscriptiq.discount.enums.DiscountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    Optional<Discount> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Page<Discount> findByStatus(DiscountStatus status, Pageable pageable);

    List<Discount> findByStatus(DiscountStatus status);
}


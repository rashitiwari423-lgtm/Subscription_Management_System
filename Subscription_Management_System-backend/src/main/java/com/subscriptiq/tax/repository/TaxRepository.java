package com.subscriptiq.tax.repository;

import com.subscriptiq.tax.entity.Tax;
import com.subscriptiq.tax.enums.TaxStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaxRepository extends JpaRepository<Tax, Long> {

    Optional<Tax> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Page<Tax> findByStatus(TaxStatus status, Pageable pageable);

    List<Tax> findByStatus(TaxStatus status);
}


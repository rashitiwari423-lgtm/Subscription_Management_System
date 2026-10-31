package com.subscriptiq.tax.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.tax.dto.CreateTaxRequest;
import com.subscriptiq.tax.dto.TaxResponse;
import com.subscriptiq.tax.dto.UpdateTaxRequest;
import com.subscriptiq.tax.dto.UpdateTaxStatusRequest;
import com.subscriptiq.tax.entity.Tax;
import com.subscriptiq.tax.enums.TaxStatus;
import com.subscriptiq.tax.repository.TaxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxServiceImpl implements TaxService {

    private final TaxRepository taxRepository;

    @Override
    @Transactional
    public TaxResponse createTax(CreateTaxRequest request) {
        String trimmedName = request.getName().trim();
        if (taxRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new BadRequestException("Tax with name '" + trimmedName + "' already exists");
        }

        Tax tax = Tax.builder()
                .name(trimmedName)
                .percentage(request.getPercentage())
                .status(TaxStatus.ACTIVE)
                .build();

        Tax saved = taxRepository.save(tax);
        log.info("Created new tax id={} name='{}'", saved.getId(), saved.getName());
        return TaxResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TaxResponse> getAllTaxes(TaxStatus status, Pageable pageable) {
        Page<Tax> page;
        if (status != null) {
            page = taxRepository.findByStatus(status, pageable);
        } else {
            page = taxRepository.findAll(pageable);
        }
        Page<TaxResponse> dtoPage = page.map(TaxResponse::fromEntity);
        return PagedResponse.fromPage(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaxResponse> getActiveTaxes() {
        return taxRepository.findByStatus(TaxStatus.ACTIVE).stream()
                .map(TaxResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaxResponse getTaxById(Long id) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found with id: " + id));
        return TaxResponse.fromEntity(tax);
    }

    @Override
    @Transactional
    public TaxResponse updateTax(Long id, UpdateTaxRequest request) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found with id: " + id));

        String trimmedName = request.getName().trim();
        if (taxRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
            throw new BadRequestException("Tax with name '" + trimmedName + "' already exists");
        }

        tax.setName(trimmedName);
        tax.setPercentage(request.getPercentage());
        if (request.getStatus() != null) {
            tax.setStatus(request.getStatus());
        }

        Tax saved = taxRepository.save(tax);
        log.info("Updated tax id={}", saved.getId());
        return TaxResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public TaxResponse updateTaxStatus(Long id, UpdateTaxStatusRequest request) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found with id: " + id));

        tax.setStatus(request.getStatus());
        Tax saved = taxRepository.save(tax);
        log.info("Updated tax id={} status to {}", saved.getId(), saved.getStatus());
        return TaxResponse.fromEntity(saved);
    }
}


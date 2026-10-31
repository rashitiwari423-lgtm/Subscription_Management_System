package com.subscriptiq.discount.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.discount.dto.CreateDiscountRequest;
import com.subscriptiq.discount.dto.DiscountResponse;
import com.subscriptiq.discount.dto.UpdateDiscountRequest;
import com.subscriptiq.discount.dto.UpdateDiscountStatusRequest;
import com.subscriptiq.discount.entity.Discount;
import com.subscriptiq.discount.enums.DiscountStatus;
import com.subscriptiq.discount.enums.DiscountType;
import com.subscriptiq.discount.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    @Override
    @Transactional
    public DiscountResponse createDiscount(CreateDiscountRequest request) {
        String trimmedName = request.getName().trim();
        if (discountRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new BadRequestException("Discount with name '" + trimmedName + "' already exists");
        }

        validateDiscountValue(request.getType(), request.getValue());

        Discount discount = Discount.builder()
                .name(trimmedName)
                .type(request.getType())
                .value(request.getValue())
                .status(DiscountStatus.ACTIVE)
                .build();

        Discount saved = discountRepository.save(discount);
        log.info("Created new discount id={} name='{}'", saved.getId(), saved.getName());
        return DiscountResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DiscountResponse> getAllDiscounts(DiscountStatus status, Pageable pageable) {
        Page<Discount> page;
        if (status != null) {
            page = discountRepository.findByStatus(status, pageable);
        } else {
            page = discountRepository.findAll(pageable);
        }
        Page<DiscountResponse> dtoPage = page.map(DiscountResponse::fromEntity);
        return PagedResponse.fromPage(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountResponse> getActiveDiscounts() {
        return discountRepository.findByStatus(DiscountStatus.ACTIVE).stream()
                .map(DiscountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountResponse getDiscountById(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));
        return DiscountResponse.fromEntity(discount);
    }

    @Override
    @Transactional
    public DiscountResponse updateDiscount(Long id, UpdateDiscountRequest request) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));

        String trimmedName = request.getName().trim();
        if (discountRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
            throw new BadRequestException("Discount with name '" + trimmedName + "' already exists");
        }

        validateDiscountValue(request.getType(), request.getValue());

        discount.setName(trimmedName);
        discount.setType(request.getType());
        discount.setValue(request.getValue());
        if (request.getStatus() != null) {
            discount.setStatus(request.getStatus());
        }

        Discount saved = discountRepository.save(discount);
        log.info("Updated discount id={}", saved.getId());
        return DiscountResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public DiscountResponse updateDiscountStatus(Long id, UpdateDiscountStatusRequest request) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with id: " + id));

        discount.setStatus(request.getStatus());
        Discount saved = discountRepository.save(discount);
        log.info("Updated discount id={} status to {}", saved.getId(), saved.getStatus());
        return DiscountResponse.fromEntity(saved);
    }

    private void validateDiscountValue(DiscountType type, BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Discount value cannot be negative");
        }
        if (type == DiscountType.PERCENTAGE && value.compareTo(new BigDecimal("100.00")) > 0) {
            throw new BadRequestException("Percentage discount value cannot exceed 100%");
        }
    }
}


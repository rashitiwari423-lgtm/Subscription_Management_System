package com.subscriptiq.discount.service;

import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.discount.dto.CreateDiscountRequest;
import com.subscriptiq.discount.dto.DiscountResponse;
import com.subscriptiq.discount.dto.UpdateDiscountRequest;
import com.subscriptiq.discount.dto.UpdateDiscountStatusRequest;
import com.subscriptiq.discount.enums.DiscountStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiscountService {

    DiscountResponse createDiscount(CreateDiscountRequest request);

    PagedResponse<DiscountResponse> getAllDiscounts(DiscountStatus status, Pageable pageable);

    List<DiscountResponse> getActiveDiscounts();

    DiscountResponse getDiscountById(Long id);

    DiscountResponse updateDiscount(Long id, UpdateDiscountRequest request);

    DiscountResponse updateDiscountStatus(Long id, UpdateDiscountStatusRequest request);
}


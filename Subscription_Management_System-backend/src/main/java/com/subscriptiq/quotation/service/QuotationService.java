package com.subscriptiq.quotation.service;

import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.quotation.dto.CreateQuotationRequest;
import com.subscriptiq.quotation.dto.QuotationResponse;
import com.subscriptiq.quotation.dto.UpdateQuotationRequest;
import com.subscriptiq.quotation.enums.QuotationStatus;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.subscription.dto.SubscriptionResponse;
import org.springframework.data.domain.Pageable;

public interface QuotationService {

    QuotationResponse createQuotation(CreateQuotationRequest request);

    PagedResponse<QuotationResponse> getQuotations(String search, QuotationStatus status, Pageable pageable);

    QuotationResponse getQuotationById(Long id, UserPrincipal principal);

    QuotationResponse updateQuotation(Long id, UpdateQuotationRequest request);

    QuotationResponse updateQuotationStatus(Long id, QuotationStatus newStatus);

    PagedResponse<QuotationResponse> getCustomerQuotations(Long customerId, UserPrincipal principal, Pageable pageable);

    SubscriptionResponse convertQuotationToSubscription(Long quotationId);
}


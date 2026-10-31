package com.subscriptiq.invoice.service;

import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.invoice.dto.CreateInvoiceRequest;
import com.subscriptiq.invoice.dto.InvoiceResponse;
import com.subscriptiq.invoice.dto.UpdateInvoiceStatusRequest;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

public interface InvoiceService {

    InvoiceResponse createInvoice(CreateInvoiceRequest request);

    InvoiceResponse createInvoiceFromSubscription(Long subscriptionId);

    PagedResponse<InvoiceResponse> getInvoices(InvoiceStatus status, Pageable pageable, UserPrincipal principal);

    InvoiceResponse getInvoiceById(Long id, UserPrincipal principal);

    InvoiceResponse updateInvoiceStatus(Long id, UpdateInvoiceStatusRequest request);

    InvoiceResponse cancelInvoice(Long id);

    PagedResponse<InvoiceResponse> getInvoicesByCustomerId(Long customerId, Pageable pageable, UserPrincipal principal);
}


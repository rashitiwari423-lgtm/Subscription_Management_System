package com.subscriptiq.payment.service;

import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.payment.dto.CreatePaymentRequest;
import com.subscriptiq.payment.dto.PaymentResponse;
import com.subscriptiq.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PagedResponse<PaymentResponse> getAllPayments(Pageable pageable);

    PaymentResponse recordPayment(Long invoiceId, CreatePaymentRequest request);

    PagedResponse<PaymentResponse> getPaymentsByInvoiceId(Long invoiceId, Pageable pageable, UserPrincipal principal);

    PaymentResponse getPaymentById(Long id, UserPrincipal principal);

    PagedResponse<PaymentResponse> getPaymentsByCustomerId(Long customerId, Pageable pageable, UserPrincipal principal);
}


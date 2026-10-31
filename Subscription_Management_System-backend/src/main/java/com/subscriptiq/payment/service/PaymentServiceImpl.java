package com.subscriptiq.payment.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import com.subscriptiq.payment.dto.CreatePaymentRequest;
import com.subscriptiq.payment.dto.PaymentResponse;
import com.subscriptiq.payment.entity.Payment;
import com.subscriptiq.payment.enums.PaymentStatus;
import com.subscriptiq.payment.repository.PaymentRepository;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> getAllPayments(Pageable pageable) {
        Page<Payment> page = paymentRepository.findAll(pageable);
        return PagedResponse.fromPage(page.map(PaymentResponse::fromEntity));
    }

    @Override
    @Transactional
    public PaymentResponse recordPayment(Long invoiceId, CreatePaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Cannot record payment for a CANCELLED invoice");
        }

        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            throw new BadRequestException("Cannot record payment for a DRAFT invoice. Please issue the invoice first.");
        }

        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getBalanceAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invoice is already fully PAID");
        }

        BigDecimal paymentAmount = request.getAmount().setScale(SCALE, ROUNDING);
        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be greater than zero");
        }

        if (paymentAmount.compareTo(invoice.getBalanceAmount()) > 0) {
            throw new BadRequestException("Payment amount (" + paymentAmount + ") cannot exceed invoice balance amount (" + invoice.getBalanceAmount() + ")");
        }

        LocalDateTime paymentDate = request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now();
        String paymentNumber = generateUniquePaymentNumber(paymentDate.toLocalDate());

        Payment payment = Payment.builder()
                .paymentNumber(paymentNumber)
                .invoice(invoice)
                .customer(invoice.getCustomer())
                .amount(paymentAmount)
                .paymentDate(paymentDate)
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.SUCCESS)
                .transactionReference(request.getTransactionReference())
                .notes(request.getNotes())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Update invoice paidAmount and balanceAmount
        BigDecimal newPaidAmount = invoice.getPaidAmount().add(paymentAmount).setScale(SCALE, ROUNDING);
        BigDecimal newBalanceAmount = invoice.getTotalAmount().subtract(newPaidAmount).setScale(SCALE, ROUNDING);

        if (newBalanceAmount.compareTo(BigDecimal.ZERO) < 0) {
            newBalanceAmount = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }

        invoice.setPaidAmount(newPaidAmount);
        invoice.setBalanceAmount(newBalanceAmount);

        if (newBalanceAmount.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);
        log.info("Recorded payment [{}] of amount [{}] for invoice [{}]. New balance: [{}] status: [{}]",
                savedPayment.getPaymentNumber(), paymentAmount, invoice.getInvoiceNumber(), newBalanceAmount, invoice.getStatus());

        return PaymentResponse.fromEntity(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> getPaymentsByInvoiceId(Long invoiceId, Pageable pageable, UserPrincipal principal) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        validateCustomerOwnership(invoice.getCustomer(), principal);

        Page<Payment> page = paymentRepository.findByInvoiceId(invoiceId, pageable);
        Page<PaymentResponse> dtoPage = page.map(PaymentResponse::fromEntity);
        return PagedResponse.fromPage(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id, UserPrincipal principal) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        validateCustomerOwnership(payment.getCustomer(), principal);

        return PaymentResponse.fromEntity(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> getPaymentsByCustomerId(Long customerId, Pageable pageable, UserPrincipal principal) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        validateCustomerOwnership(customer, principal);

        Page<Payment> page = paymentRepository.findByCustomerId(customerId, pageable);
        Page<PaymentResponse> dtoPage = page.map(PaymentResponse::fromEntity);
        return PagedResponse.fromPage(dtoPage);
    }

    private void validateCustomerOwnership(Customer customer, UserPrincipal principal) {
        if (principal != null && principal.getRole() == UserRole.CUSTOMER) {
            if (customer.getUser() == null || !customer.getUser().getId().equals(principal.getId())) {
                throw new AccessDeniedException("Access denied: You do not have permission to view other customer records.");
            }
        }
    }

    private String generateUniquePaymentNumber(LocalDate date) {
        String datePart = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        String number;
        int attempts = 0;
        do {
            int randomSuffix = 1000 + RANDOM.nextInt(9000);
            number = "PAY-" + datePart + "-" + randomSuffix;
            attempts++;
            if (attempts > 50) {
                number = "PAY-" + datePart + "-" + (System.currentTimeMillis() % 100000);
            }
        } while (paymentRepository.existsByPaymentNumber(number));
        return number;
    }
}


package com.subscriptiq.invoice.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.invoice.dto.CreateInvoiceItemRequest;
import com.subscriptiq.invoice.dto.CreateInvoiceRequest;
import com.subscriptiq.invoice.dto.InvoiceResponse;
import com.subscriptiq.invoice.dto.UpdateInvoiceStatusRequest;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.entity.InvoiceItem;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.repository.PlanRepository;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import com.subscriptiq.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final BillingCalculationService billingCalculationService;

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        Subscription subscription = null;
        if (request.getSubscriptionId() != null) {
            subscription = subscriptionRepository.findById(request.getSubscriptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + request.getSubscriptionId()));
        }

        if (request.getDueDate().isBefore(request.getInvoiceDate())) {
            throw new BadRequestException("Due date cannot be before invoice date");
        }

        String invoiceNumber = generateUniqueInvoiceNumber(request.getInvoiceDate());

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .customer(customer)
                .subscription(subscription)
                .invoiceDate(request.getInvoiceDate())
                .billingPeriodStart(request.getBillingPeriodStart())
                .billingPeriodEnd(request.getBillingPeriodEnd())
                .dueDate(request.getDueDate())
                .status(InvoiceStatus.DRAFT)
                .notes(request.getNotes())
                .items(new ArrayList<>())
                .build();

        for (CreateInvoiceItemRequest itemRequest : request.getItems()) {
            Plan plan = null;
            if (itemRequest.getPlanId() != null) {
                plan = planRepository.findById(itemRequest.getPlanId())
                        .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + itemRequest.getPlanId()));
            }

            InvoiceItem item = InvoiceItem.builder()
                    .plan(plan)
                    .description(itemRequest.getDescription() != null ? itemRequest.getDescription() : (plan != null ? plan.getName() : null))
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .discountAmount(itemRequest.getDiscountAmount() != null ? itemRequest.getDiscountAmount() : BigDecimal.ZERO)
                    .taxAmount(itemRequest.getTaxAmount() != null ? itemRequest.getTaxAmount() : BigDecimal.ZERO)
                    .build();

            invoice.addItem(item);
        }

        billingCalculationService.recalculateInvoiceTotals(invoice);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Created invoice [{}] for customer [{}] with total amount [{}]",
                saved.getInvoiceNumber(), customer.getCompanyName(), saved.getTotalAmount());
        return InvoiceResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse createInvoiceFromSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        Customer customer = subscription.getCustomer();
        LocalDate invoiceDate = LocalDate.now();
        LocalDate dueDate = invoiceDate.plusDays(15);

        String invoiceNumber = generateUniqueInvoiceNumber(invoiceDate);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .customer(customer)
                .subscription(subscription)
                .invoiceDate(invoiceDate)
                .dueDate(dueDate)
                .status(InvoiceStatus.ISSUED)
                .notes("Generated for subscription " + subscription.getSubscriptionNumber())
                .items(new ArrayList<>())
                .build();

        InvoiceItem item = InvoiceItem.builder()
                .plan(subscription.getPlan())
                .description("Subscription: " + subscription.getPlan().getName())
                .quantity(subscription.getQuantity())
                .unitPrice(subscription.getUnitPrice())
                .discountAmount(subscription.getDiscountAmount() != null ? subscription.getDiscountAmount() : BigDecimal.ZERO)
                .taxAmount(subscription.getTaxAmount() != null ? subscription.getTaxAmount() : BigDecimal.ZERO)
                .build();

        invoice.addItem(item);
        billingCalculationService.recalculateInvoiceTotals(invoice);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Created invoice [{}] from subscription [{}] for customer [{}]",
                saved.getInvoiceNumber(), subscription.getSubscriptionNumber(), customer.getCompanyName());
        return InvoiceResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InvoiceResponse> getInvoices(InvoiceStatus status, Pageable pageable, UserPrincipal principal) {
        Page<Invoice> page;
        if (principal != null && principal.getRole() == UserRole.CUSTOMER) {
            Customer customer = customerRepository.findByUserId(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for authenticated user"));

            if (status != null) {
                page = invoiceRepository.findByCustomerIdAndStatus(customer.getId(), status, pageable);
            } else {
                page = invoiceRepository.findByCustomerId(customer.getId(), pageable);
            }
        } else {
            if (status != null) {
                page = invoiceRepository.findByStatus(status, pageable);
            } else {
                page = invoiceRepository.findAll(pageable);
            }
        }

        // Apply dynamic overdue check
        LocalDate today = LocalDate.now();
        for (Invoice invoice : page.getContent()) {
            if (billingCalculationService.isOverdue(invoice, today) && invoice.getStatus() != InvoiceStatus.OVERDUE) {
                invoice.setStatus(InvoiceStatus.OVERDUE);
            }
        }

        Page<InvoiceResponse> dtoPage = page.map(InvoiceResponse::fromEntity);
        return PagedResponse.fromPage(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id, UserPrincipal principal) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        validateCustomerOwnership(invoice.getCustomer(), principal);

        if (billingCalculationService.isOverdue(invoice, LocalDate.now()) && invoice.getStatus() != InvoiceStatus.OVERDUE) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
        }

        return InvoiceResponse.fromEntity(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoiceStatus(Long id, UpdateInvoiceStatusRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        InvoiceStatus targetStatus = request.getStatus();
        InvoiceStatus currentStatus = invoice.getStatus();

        if (currentStatus == targetStatus) {
            return InvoiceResponse.fromEntity(invoice);
        }

        if (currentStatus == InvoiceStatus.PAID) {
            throw new BadRequestException("Cannot change status of a PAID invoice");
        }

        if (currentStatus == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Cannot change status of a CANCELLED invoice");
        }

        if (targetStatus == InvoiceStatus.CANCELLED) {
            return cancelInvoice(id);
        }

        // Validating lifecycle transitions
        if (currentStatus == InvoiceStatus.DRAFT && targetStatus != InvoiceStatus.ISSUED) {
            throw new BadRequestException("Draft invoices can only transition to ISSUED or CANCELLED");
        }

        invoice.setStatus(targetStatus);
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Updated status of invoice [{}] to [{}]", saved.getInvoiceNumber(), saved.getStatus());
        return InvoiceResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Cannot cancel an invoice that is already PAID");
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Invoice is already CANCELLED");
        }

        if (invoice.getPaidAmount() != null && invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("Cannot cancel an invoice with recorded payments. Paid amount: " + invoice.getPaidAmount());
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Cancelled invoice [{}]", saved.getInvoiceNumber());
        return InvoiceResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InvoiceResponse> getInvoicesByCustomerId(Long customerId, Pageable pageable, UserPrincipal principal) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        validateCustomerOwnership(customer, principal);

        Page<Invoice> page = invoiceRepository.findByCustomerId(customerId, pageable);
        LocalDate today = LocalDate.now();
        for (Invoice invoice : page.getContent()) {
            if (billingCalculationService.isOverdue(invoice, today) && invoice.getStatus() != InvoiceStatus.OVERDUE) {
                invoice.setStatus(InvoiceStatus.OVERDUE);
            }
        }

        Page<InvoiceResponse> dtoPage = page.map(InvoiceResponse::fromEntity);
        return PagedResponse.fromPage(dtoPage);
    }

    private void validateCustomerOwnership(Customer customer, UserPrincipal principal) {
        if (principal != null && principal.getRole() == UserRole.CUSTOMER) {
            if (customer.getUser() == null || !customer.getUser().getId().equals(principal.getId())) {
                throw new AccessDeniedException("Access denied: You do not have permission to view other customer records.");
            }
        }
    }

    private String generateUniqueInvoiceNumber(LocalDate date) {
        String datePart = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        String number;
        int attempts = 0;
        do {
            int randomSuffix = 1000 + RANDOM.nextInt(9000);
            number = "INV-" + datePart + "-" + randomSuffix;
            attempts++;
            if (attempts > 50) {
                number = "INV-" + datePart + "-" + (System.currentTimeMillis() % 100000);
            }
        } while (invoiceRepository.existsByInvoiceNumber(number));
        return number;
    }
}


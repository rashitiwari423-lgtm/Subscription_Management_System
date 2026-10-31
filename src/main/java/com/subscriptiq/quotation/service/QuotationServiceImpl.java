package com.subscriptiq.quotation.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.enums.PlanStatus;
import com.subscriptiq.plan.repository.PlanRepository;
import com.subscriptiq.quotation.dto.CreateQuotationItemRequest;
import com.subscriptiq.quotation.dto.CreateQuotationRequest;
import com.subscriptiq.quotation.dto.QuotationItemResponse;
import com.subscriptiq.quotation.dto.QuotationResponse;
import com.subscriptiq.quotation.dto.UpdateQuotationRequest;
import com.subscriptiq.quotation.entity.Quotation;
import com.subscriptiq.quotation.entity.QuotationItem;
import com.subscriptiq.quotation.enums.QuotationStatus;
import com.subscriptiq.quotation.repository.QuotationRepository;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.subscription.dto.SubscriptionResponse;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import com.subscriptiq.user.enums.UserRole;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final QuotationCalculationService calculationService;

    private static final Random RANDOM = new Random();

    @Override
    @Transactional
    public QuotationResponse createQuotation(CreateQuotationRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        LocalDate quotationDate = request.getQuotationDate() != null ? request.getQuotationDate() : LocalDate.now();
        LocalDate validUntil = request.getValidUntil();

        if (validUntil.isBefore(quotationDate)) {
            throw new BadRequestException("validUntil date cannot be before quotationDate");
        }

        String quotationNumber = generateUniqueQuotationNumber(quotationDate);

        Quotation quotation = Quotation.builder()
                .quotationNumber(quotationNumber)
                .customer(customer)
                .status(QuotationStatus.DRAFT)
                .quotationDate(quotationDate)
                .validUntil(validUntil)
                .notes(request.getNotes())
                .items(new ArrayList<>())
                .build();

        for (CreateQuotationItemRequest itemRequest : request.getItems()) {
            Plan plan = planRepository.findById(itemRequest.getPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + itemRequest.getPlanId()));

            if (plan.getStatus() != PlanStatus.ACTIVE) {
                throw new BadRequestException("Plan with id " + itemRequest.getPlanId() + " is not ACTIVE and cannot be quoted");
            }

            QuotationItem item = QuotationItem.builder()
                    .plan(plan)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(plan.getPrice()) // PRICING SNAPSHOT FROM ACTIVE PLAN
                    .discountAmount(itemRequest.getDiscountAmount())
                    .taxAmount(itemRequest.getTaxAmount())
                    .build();

            quotation.addItem(item);
        }

        calculationService.recalculateQuotation(quotation);

        Quotation saved = quotationRepository.save(quotation);
        log.info("Created quotation [{}] for customer [{}] with total amount [{}]",
                saved.getQuotationNumber(), customer.getCompanyName(), saved.getTotalAmount());

        return mapToQuotationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<QuotationResponse> getQuotations(String search, QuotationStatus status, Pageable pageable) {
        Specification<Quotation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate numMatch = cb.like(cb.lower(root.get("quotationNumber")), pattern);
                Predicate companyMatch = cb.like(cb.lower(root.get("customer").get("companyName")), pattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("customer").get("email")), pattern);
                predicates.add(cb.or(numMatch, companyMatch, emailMatch));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<QuotationResponse> page = quotationRepository.findAll(spec, pageable).map(this::mapToQuotationResponse);
        return PagedResponse.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public QuotationResponse getQuotationById(Long id, UserPrincipal principal) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + id));

        validateCustomerOwnership(quotation.getCustomer(), principal);
        return mapToQuotationResponse(quotation);
    }

    @Override
    @Transactional
    public QuotationResponse updateQuotation(Long id, UpdateQuotationRequest request) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + id));

        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new BadRequestException("Only quotations in DRAFT status can be modified. Current status: " + quotation.getStatus());
        }

        if (request.getValidUntil() != null) {
            if (request.getValidUntil().isBefore(quotation.getQuotationDate())) {
                throw new BadRequestException("validUntil date cannot be before quotationDate");
            }
            quotation.setValidUntil(request.getValidUntil());
        }

        if (request.getNotes() != null) {
            quotation.setNotes(request.getNotes());
        }

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            quotation.getItems().clear();

            for (CreateQuotationItemRequest itemRequest : request.getItems()) {
                Plan plan = planRepository.findById(itemRequest.getPlanId())
                        .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + itemRequest.getPlanId()));

                if (plan.getStatus() != PlanStatus.ACTIVE) {
                    throw new BadRequestException("Plan with id " + itemRequest.getPlanId() + " is not ACTIVE");
                }

                QuotationItem item = QuotationItem.builder()
                        .plan(plan)
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(plan.getPrice()) // Pricing snapshot
                        .discountAmount(itemRequest.getDiscountAmount())
                        .taxAmount(itemRequest.getTaxAmount())
                        .build();

                quotation.addItem(item);
            }

            calculationService.recalculateQuotation(quotation);
        }

        Quotation updated = quotationRepository.save(quotation);
        log.info("Updated quotation [{}]", updated.getQuotationNumber());
        return mapToQuotationResponse(updated);
    }

    @Override
    @Transactional
    public QuotationResponse updateQuotationStatus(Long id, QuotationStatus newStatus) {
        Quotation quotation = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + id));

        QuotationStatus currentStatus = quotation.getStatus();

        // Expired check: If validUntil is before today, mark EXPIRED and block ACCEPTED/SENT
        if (LocalDate.now().isAfter(quotation.getValidUntil())) {
            if (currentStatus != QuotationStatus.EXPIRED) {
                quotation.setStatus(QuotationStatus.EXPIRED);
                quotationRepository.save(quotation);
            }
            if (newStatus == QuotationStatus.ACCEPTED || newStatus == QuotationStatus.SENT) {
                throw new BadRequestException("Quotation has expired on " + quotation.getValidUntil() + " and cannot be accepted");
            }
        }

        // Controlled Lifecycle Transitions
        boolean allowed = switch (currentStatus) {
            case DRAFT -> (newStatus == QuotationStatus.SENT || newStatus == QuotationStatus.REJECTED);
            case SENT -> (newStatus == QuotationStatus.ACCEPTED || newStatus == QuotationStatus.REJECTED);
            default -> false;
        };

        if (!allowed) {
            throw new BadRequestException(String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }

        quotation.setStatus(newStatus);
        Quotation saved = quotationRepository.save(quotation);
        log.info("Transitioned quotation [{}] from [{}] to [{}]", saved.getQuotationNumber(), currentStatus, newStatus);
        return mapToQuotationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<QuotationResponse> getCustomerQuotations(Long customerId, UserPrincipal principal, Pageable pageable) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        validateCustomerOwnership(customer, principal);

        Page<QuotationResponse> page = quotationRepository.findByCustomerId(customerId, pageable).map(this::mapToQuotationResponse);
        return PagedResponse.fromPage(page);
    }

    @Override
    @Transactional
    public SubscriptionResponse convertQuotationToSubscription(Long quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + quotationId));

        // Check if expired
        if (LocalDate.now().isAfter(quotation.getValidUntil())) {
            if (quotation.getStatus() != QuotationStatus.EXPIRED) {
                quotation.setStatus(QuotationStatus.EXPIRED);
                quotationRepository.save(quotation);
            }
            throw new BadRequestException("Cannot convert an EXPIRED quotation");
        }

        // Status validation
        if (quotation.getStatus() == QuotationStatus.CONVERTED) {
            throw new BadRequestException("Quotation has already been converted to a subscription");
        }
        if (quotation.getStatus() == QuotationStatus.REJECTED) {
            throw new BadRequestException("Cannot convert a REJECTED quotation");
        }
        if (quotation.getStatus() == QuotationStatus.DRAFT) {
            throw new BadRequestException("Only ACCEPTED quotations can be converted to a subscription");
        }
        if (quotation.getStatus() != QuotationStatus.ACCEPTED) {
            throw new BadRequestException("Only ACCEPTED quotations can be converted to a subscription. Current status: " + quotation.getStatus());
        }

        // Ensure quotation items exist
        if (quotation.getItems() == null || quotation.getItems().isEmpty()) {
            throw new BadRequestException("Quotation has no line items to convert");
        }

        // Prevent race condition / duplicate conversion
        List<Subscription> existingSubscriptions = subscriptionRepository.findByQuotationId(quotationId);
        if (!existingSubscriptions.isEmpty()) {
            throw new BadRequestException("Quotation has already been converted to a subscription");
        }

        LocalDate startDate = LocalDate.now();

        // Convert the primary quotation line item into a recurring subscription
        QuotationItem primaryItem = quotation.getItems().get(0);
        Plan plan = primaryItem.getPlan();

        LocalDate nextBillingDate = calculationService.calculateNextBillingDate(startDate, plan.getBillingCycle());
        String subscriptionNumber = generateUniqueSubscriptionNumber(startDate);

        Subscription subscription = Subscription.builder()
                .subscriptionNumber(subscriptionNumber)
                .customer(quotation.getCustomer())
                .plan(plan)
                .quotation(quotation)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(startDate)
                .nextBillingDate(nextBillingDate)
                .quantity(primaryItem.getQuantity())
                .unitPrice(primaryItem.getUnitPrice()) // PRICING SNAPSHOT PRESERVED
                .discountAmount(primaryItem.getDiscountAmount()) // PRICING SNAPSHOT PRESERVED
                .taxAmount(primaryItem.getTaxAmount()) // PRICING SNAPSHOT PRESERVED
                .totalAmount(primaryItem.getTotalAmount()) // PRICING SNAPSHOT PRESERVED
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // If quotation had additional items, persist corresponding subscriptions
        for (int i = 1; i < quotation.getItems().size(); i++) {
            QuotationItem extraItem = quotation.getItems().get(i);
            Plan extraPlan = extraItem.getPlan();
            LocalDate extraNextBilling = calculationService.calculateNextBillingDate(startDate, extraPlan.getBillingCycle());
            String extraSubNumber = generateUniqueSubscriptionNumber(startDate);

            Subscription extraSub = Subscription.builder()
                    .subscriptionNumber(extraSubNumber)
                    .customer(quotation.getCustomer())
                    .plan(extraPlan)
                    .quotation(quotation)
                    .status(SubscriptionStatus.ACTIVE)
                    .startDate(startDate)
                    .nextBillingDate(extraNextBilling)
                    .quantity(extraItem.getQuantity())
                    .unitPrice(extraItem.getUnitPrice())
                    .discountAmount(extraItem.getDiscountAmount())
                    .taxAmount(extraItem.getTaxAmount())
                    .totalAmount(extraItem.getTotalAmount())
                    .build();

            subscriptionRepository.save(extraSub);
        }

        // Transition quotation to CONVERTED
        quotation.setStatus(QuotationStatus.CONVERTED);
        quotationRepository.save(quotation);

        log.info("Successfully converted quotation [{}] to subscription [{}] for customer [{}]",
                quotation.getQuotationNumber(), savedSubscription.getSubscriptionNumber(), quotation.getCustomer().getCompanyName());

        return mapToSubscriptionResponse(savedSubscription);
    }

    private void validateCustomerOwnership(Customer customer, UserPrincipal principal) {
        if (principal != null && principal.getRole() == UserRole.CUSTOMER) {
            if (customer.getUser() == null || !customer.getUser().getId().equals(principal.getId())) {
                throw new AccessDeniedException("Access denied: You do not have permission to view other customer records.");
            }
        }
    }

    private String generateUniqueQuotationNumber(LocalDate date) {
        String datePart = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        String number;
        int attempts = 0;
        do {
            int randomSuffix = 1000 + RANDOM.nextInt(9000);
            number = "QT-" + datePart + "-" + randomSuffix;
            attempts++;
            if (attempts > 50) {
                number = "QT-" + datePart + "-" + System.currentTimeMillis() % 100000;
            }
        } while (quotationRepository.existsByQuotationNumber(number));
        return number;
    }

    private String generateUniqueSubscriptionNumber(LocalDate date) {
        String datePart = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        String number;
        int attempts = 0;
        do {
            int randomSuffix = 1000 + RANDOM.nextInt(9000);
            number = "SUB-" + datePart + "-" + randomSuffix;
            attempts++;
            if (attempts > 50) {
                number = "SUB-" + datePart + "-" + System.currentTimeMillis() % 100000;
            }
        } while (subscriptionRepository.existsBySubscriptionNumber(number));
        return number;
    }

    private QuotationResponse mapToQuotationResponse(Quotation quotation) {
        List<QuotationItemResponse> itemResponses = new ArrayList<>();
        if (quotation.getItems() != null) {
            for (QuotationItem item : quotation.getItems()) {
                itemResponses.add(QuotationItemResponse.builder()
                        .id(item.getId())
                        .planId(item.getPlan() != null ? item.getPlan().getId() : null)
                        .planName(item.getPlan() != null ? item.getPlan().getName() : null)
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discountAmount(item.getDiscountAmount())
                        .taxAmount(item.getTaxAmount())
                        .totalAmount(item.getTotalAmount())
                        .build());
            }
        }

        return QuotationResponse.builder()
                .id(quotation.getId())
                .quotationNumber(quotation.getQuotationNumber())
                .customerId(quotation.getCustomer() != null ? quotation.getCustomer().getId() : null)
                .customerCompanyName(quotation.getCustomer() != null ? quotation.getCustomer().getCompanyName() : null)
                .customerEmail(quotation.getCustomer() != null ? quotation.getCustomer().getEmail() : null)
                .status(quotation.getStatus())
                .quotationDate(quotation.getQuotationDate())
                .validUntil(quotation.getValidUntil())
                .subtotal(quotation.getSubtotal())
                .discountAmount(quotation.getDiscountAmount())
                .taxAmount(quotation.getTaxAmount())
                .totalAmount(quotation.getTotalAmount())
                .notes(quotation.getNotes())
                .items(itemResponses)
                .createdAt(quotation.getCreatedAt())
                .updatedAt(quotation.getUpdatedAt())
                .build();
    }

    private SubscriptionResponse mapToSubscriptionResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .subscriptionNumber(subscription.getSubscriptionNumber())
                .customerId(subscription.getCustomer() != null ? subscription.getCustomer().getId() : null)
                .customerCompanyName(subscription.getCustomer() != null ? subscription.getCustomer().getCompanyName() : null)
                .planId(subscription.getPlan() != null ? subscription.getPlan().getId() : null)
                .planName(subscription.getPlan() != null ? subscription.getPlan().getName() : null)
                .billingCycle(subscription.getPlan() != null ? subscription.getPlan().getBillingCycle() : null)
                .quotationId(subscription.getQuotation() != null ? subscription.getQuotation().getId() : null)
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .nextBillingDate(subscription.getNextBillingDate())
                .quantity(subscription.getQuantity())
                .unitPrice(subscription.getUnitPrice())
                .discountAmount(subscription.getDiscountAmount())
                .taxAmount(subscription.getTaxAmount())
                .totalAmount(subscription.getTotalAmount())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
}


package com.subscriptiq.quotation.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.enums.BillingCycle;
import com.subscriptiq.plan.enums.PlanStatus;
import com.subscriptiq.plan.repository.PlanRepository;
import com.subscriptiq.quotation.dto.CreateQuotationItemRequest;
import com.subscriptiq.quotation.dto.CreateQuotationRequest;
import com.subscriptiq.quotation.dto.QuotationResponse;
import com.subscriptiq.quotation.entity.Quotation;
import com.subscriptiq.quotation.entity.QuotationItem;
import com.subscriptiq.quotation.enums.QuotationStatus;
import com.subscriptiq.quotation.repository.QuotationRepository;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.subscription.dto.SubscriptionResponse;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import com.subscriptiq.user.entity.User;
import com.subscriptiq.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuotationServiceTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private QuotationCalculationService calculationService;
    private QuotationServiceImpl quotationService;

    private Customer testCustomer;
    private Plan testPlan;
    private User customerUser;

    @BeforeEach
    void setUp() {
        calculationService = new QuotationCalculationServiceImpl();
        quotationService = new QuotationServiceImpl(
                quotationRepository,
                customerRepository,
                planRepository,
                subscriptionRepository,
                calculationService
        );

        customerUser = User.builder().id(99L).email("client@test.com").role(UserRole.CUSTOMER).build();
        testCustomer = Customer.builder().id(1L).companyName("Acme Corp").email("billing@acme.com").user(customerUser).build();
        testPlan = Plan.builder().id(10L).name("Cloud Basic").billingCycle(BillingCycle.MONTHLY).price(new BigDecimal("199.00")).status(PlanStatus.ACTIVE).build();
    }

    @Test
    @DisplayName("Create quotation snapshots plan price and calculates total")
    void testCreateQuotation_Success() {
        CreateQuotationRequest request = CreateQuotationRequest.builder()
                .customerId(1L)
                .quotationDate(LocalDate.now())
                .validUntil(LocalDate.now().plusDays(15))
                .notes("Standard contract")
                .items(List.of(
                        CreateQuotationItemRequest.builder().planId(10L).quantity(2).discountAmount(new BigDecimal("10.00")).taxAmount(new BigDecimal("5.00")).build()
                ))
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(planRepository.findById(10L)).thenReturn(Optional.of(testPlan));
        when(quotationRepository.existsByQuotationNumber(anyString())).thenReturn(false);
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(invocation -> {
            Quotation q = invocation.getArgument(0);
            q.setId(100L);
            return q;
        });

        QuotationResponse response = quotationService.createQuotation(request);

        assertNotNull(response);
        assertEquals(QuotationStatus.DRAFT, response.getStatus());
        assertEquals(new BigDecimal("398.00"), response.getSubtotal()); // 199 * 2
        assertEquals(new BigDecimal("393.00"), response.getTotalAmount()); // 398 - 10 + 5
        verify(quotationRepository).save(any(Quotation.class));
    }

    @Test
    @DisplayName("Create quotation with inactive plan throws BadRequestException")
    void testCreateQuotation_InactivePlan_Throws() {
        testPlan.setStatus(PlanStatus.INACTIVE);
        CreateQuotationRequest request = CreateQuotationRequest.builder()
                .customerId(1L)
                .validUntil(LocalDate.now().plusDays(10))
                .items(List.of(CreateQuotationItemRequest.builder().planId(10L).quantity(1).build()))
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(planRepository.findById(10L)).thenReturn(Optional.of(testPlan));

        assertThrows(BadRequestException.class, () -> quotationService.createQuotation(request));
    }

    @Test
    @DisplayName("Quotation validUntil before quotationDate throws BadRequestException")
    void testCreateQuotation_InvalidDates_Throws() {
        CreateQuotationRequest request = CreateQuotationRequest.builder()
                .customerId(1L)
                .quotationDate(LocalDate.now())
                .validUntil(LocalDate.now().minusDays(1))
                .items(List.of(CreateQuotationItemRequest.builder().planId(10L).quantity(1).build()))
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        assertThrows(BadRequestException.class, () -> quotationService.createQuotation(request));
    }

    @Test
    @DisplayName("Transition status DRAFT to SENT and SENT to ACCEPTED")
    void testStatusTransition_Success() {
        Quotation quotation = Quotation.builder()
                .id(1L)
                .status(QuotationStatus.DRAFT)
                .validUntil(LocalDate.now().plusDays(10))
                .build();

        when(quotationRepository.findById(1L)).thenReturn(Optional.of(quotation));
        when(quotationRepository.save(any(Quotation.class))).thenAnswer(inv -> inv.getArgument(0));

        QuotationResponse sentResponse = quotationService.updateQuotationStatus(1L, QuotationStatus.SENT);
        assertEquals(QuotationStatus.SENT, sentResponse.getStatus());

        QuotationResponse acceptedResponse = quotationService.updateQuotationStatus(1L, QuotationStatus.ACCEPTED);
        assertEquals(QuotationStatus.ACCEPTED, acceptedResponse.getStatus());
    }

    @Test
    @DisplayName("Invalid status transition DRAFT directly to ACCEPTED throws BadRequestException")
    void testInvalidStatusTransition_Throws() {
        Quotation quotation = Quotation.builder()
                .id(1L)
                .status(QuotationStatus.DRAFT)
                .validUntil(LocalDate.now().plusDays(10))
                .build();

        when(quotationRepository.findById(1L)).thenReturn(Optional.of(quotation));

        assertThrows(BadRequestException.class, () ->
                quotationService.updateQuotationStatus(1L, QuotationStatus.ACCEPTED));
    }

    @Test
    @DisplayName("Expired quotation cannot be accepted")
    void testExpiredQuotation_CannotBeAccepted() {
        Quotation quotation = Quotation.builder()
                .id(1L)
                .status(QuotationStatus.SENT)
                .validUntil(LocalDate.now().minusDays(2)) // Expired!
                .build();

        when(quotationRepository.findById(1L)).thenReturn(Optional.of(quotation));

        assertThrows(BadRequestException.class, () ->
                quotationService.updateQuotationStatus(1L, QuotationStatus.ACCEPTED));
    }

    @Test
    @DisplayName("Convert ACCEPTED quotation to subscription")
    void testConvertQuotation_Success() {
        QuotationItem item = QuotationItem.builder()
                .plan(testPlan)
                .quantity(3)
                .unitPrice(new BigDecimal("199.00"))
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("597.00"))
                .build();

        Quotation quotation = Quotation.builder()
                .id(1L)
                .quotationNumber("QT-20261031-1001")
                .customer(testCustomer)
                .status(QuotationStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(10))
                .items(new ArrayList<>(List.of(item)))
                .build();

        when(quotationRepository.findById(1L)).thenReturn(Optional.of(quotation));
        when(subscriptionRepository.findByQuotationId(1L)).thenReturn(Collections.emptyList());
        when(subscriptionRepository.existsBySubscriptionNumber(anyString())).thenReturn(false);
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> {
            Subscription s = inv.getArgument(0);
            s.setId(500L);
            return s;
        });

        SubscriptionResponse response = quotationService.convertQuotationToSubscription(1L);

        assertNotNull(response);
        assertEquals(SubscriptionStatus.ACTIVE, response.getStatus());
        assertEquals(new BigDecimal("199.00"), response.getUnitPrice());
        assertEquals(3, response.getQuantity());
        assertEquals(QuotationStatus.CONVERTED, quotation.getStatus());
    }

    @Test
    @DisplayName("Cannot convert already CONVERTED quotation")
    void testConvertQuotation_AlreadyConverted_Throws() {
        Quotation quotation = Quotation.builder()
                .id(1L)
                .status(QuotationStatus.CONVERTED)
                .validUntil(LocalDate.now().plusDays(10))
                .build();

        when(quotationRepository.findById(1L)).thenReturn(Optional.of(quotation));

        assertThrows(BadRequestException.class, () -> quotationService.convertQuotationToSubscription(1L));
    }

    @Test
    @DisplayName("Cannot convert REJECTED quotation")
    void testConvertQuotation_Rejected_Throws() {
        Quotation quotation = Quotation.builder()
                .id(1L)
                .status(QuotationStatus.REJECTED)
                .validUntil(LocalDate.now().plusDays(10))
                .build();

        when(quotationRepository.findById(1L)).thenReturn(Optional.of(quotation));

        assertThrows(BadRequestException.class, () -> quotationService.convertQuotationToSubscription(1L));
    }

    @Test
    @DisplayName("Customer cannot access another customer's quotation")
    void testCustomerOwnership_Forbidden() {
        Quotation quotation = Quotation.builder()
                .id(1L)
                .customer(testCustomer) // linked to userId 99L
                .build();

        UserPrincipal otherCustomerPrincipal = UserPrincipal.builder()
                .id(101L) // Different user!
                .email("other@client.com")
                .role(UserRole.CUSTOMER)
                .build();

        when(quotationRepository.findById(1L)).thenReturn(Optional.of(quotation));

        assertThrows(AccessDeniedException.class, () ->
                quotationService.getQuotationById(1L, otherCustomerPrincipal));
    }
}


package com.subscriptiq.subscription.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.enums.BillingCycle;
import com.subscriptiq.quotation.service.QuotationCalculationService;
import com.subscriptiq.quotation.service.QuotationCalculationServiceImpl;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CustomerRepository customerRepository;

    private QuotationCalculationService calculationService;
    private SubscriptionServiceImpl subscriptionService;

    private Subscription testSubscription;
    private Customer testCustomer;
    private Plan testPlan;
    private User customerUser;

    @BeforeEach
    void setUp() {
        calculationService = new QuotationCalculationServiceImpl();
        subscriptionService = new SubscriptionServiceImpl(
                subscriptionRepository,
                customerRepository,
                calculationService
        );

        customerUser = User.builder().id(99L).email("client@test.com").role(UserRole.CUSTOMER).build();
        testCustomer = Customer.builder().id(1L).companyName("Acme Corp").email("billing@acme.com").user(customerUser).build();
        testPlan = Plan.builder().id(10L).name("Cloud Basic").billingCycle(BillingCycle.MONTHLY).price(new BigDecimal("199.00")).build();

        testSubscription = Subscription.builder()
                .id(1L)
                .subscriptionNumber("SUB-20261031-1001")
                .customer(testCustomer)
                .plan(testPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .nextBillingDate(LocalDate.of(2026, 2, 1))
                .quantity(1)
                .unitPrice(new BigDecimal("199.00"))
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("199.00"))
                .build();
    }

    @Test
    @DisplayName("Pause ACTIVE subscription transitions to PAUSED")
    void testPauseSubscription_Success() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionResponse response = subscriptionService.pauseSubscription(1L);

        assertEquals(SubscriptionStatus.PAUSED, response.getStatus());
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    @DisplayName("Pausing non-ACTIVE subscription throws BadRequestException")
    void testPauseSubscription_NotActive_Throws() {
        testSubscription.setStatus(SubscriptionStatus.PAUSED);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));

        assertThrows(BadRequestException.class, () -> subscriptionService.pauseSubscription(1L));
    }

    @Test
    @DisplayName("Resume PAUSED subscription transitions back to ACTIVE")
    void testResumeSubscription_Success() {
        testSubscription.setStatus(SubscriptionStatus.PAUSED);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionResponse response = subscriptionService.resumeSubscription(1L);

        assertEquals(SubscriptionStatus.ACTIVE, response.getStatus());
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    @DisplayName("Resuming non-PAUSED subscription throws BadRequestException")
    void testResumeSubscription_NotPaused_Throws() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));

        assertThrows(BadRequestException.class, () -> subscriptionService.resumeSubscription(1L));
    }

    @Test
    @DisplayName("Close subscription sets status to CLOSED and sets endDate")
    void testCloseSubscription_Success() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionResponse response = subscriptionService.closeSubscription(1L);

        assertEquals(SubscriptionStatus.CLOSED, response.getStatus());
        assertNotNull(response.getEndDate());
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    @DisplayName("Closing already CLOSED subscription throws BadRequestException")
    void testCloseSubscription_AlreadyClosed_Throws() {
        testSubscription.setStatus(SubscriptionStatus.CLOSED);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));

        assertThrows(BadRequestException.class, () -> subscriptionService.closeSubscription(1L));
    }

    @Test
    @DisplayName("Renew ACTIVE subscription advances nextBillingDate based on billing cycle")
    void testRenewSubscription_Success() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        // Initial next billing date: 2026-02-01, Monthly cycle -> next should be 2026-03-01
        SubscriptionResponse response = subscriptionService.renewSubscription(1L);

        assertEquals(LocalDate.of(2026, 3, 1), response.getNextBillingDate());
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    @DisplayName("Customer cannot access another customer's subscription")
    void testCustomerOwnership_Forbidden() {
        UserPrincipal otherCustomerPrincipal = UserPrincipal.builder()
                .id(999L) // Different customer
                .email("intruder@example.com")
                .role(UserRole.CUSTOMER)
                .build();

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));

        assertThrows(AccessDeniedException.class, () ->
                subscriptionService.getSubscriptionById(1L, otherCustomerPrincipal));
    }
}


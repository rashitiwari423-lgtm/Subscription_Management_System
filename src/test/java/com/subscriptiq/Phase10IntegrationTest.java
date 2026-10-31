package com.subscriptiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.enums.CustomerStatus;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import com.subscriptiq.optimization.entity.OptimizationInsight;
import com.subscriptiq.optimization.enums.OptimizationRecommendation;
import com.subscriptiq.optimization.enums.OptimizationStatus;
import com.subscriptiq.optimization.repository.OptimizationInsightRepository;
import com.subscriptiq.payment.entity.Payment;
import com.subscriptiq.payment.enums.PaymentMethod;
import com.subscriptiq.payment.enums.PaymentStatus;
import com.subscriptiq.payment.repository.PaymentRepository;
import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.enums.BillingCycle;
import com.subscriptiq.plan.enums.PlanStatus;
import com.subscriptiq.plan.repository.PlanRepository;
import com.subscriptiq.product.entity.Product;
import com.subscriptiq.product.entity.ProductVariant;
import com.subscriptiq.product.enums.ProductStatus;
import com.subscriptiq.product.enums.VariantStatus;
import com.subscriptiq.product.repository.ProductRepository;
import com.subscriptiq.product.repository.ProductVariantRepository;
import com.subscriptiq.security.JwtService;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import com.subscriptiq.usage.entity.UsageRecord;
import com.subscriptiq.usage.repository.UsageRecordRepository;
import com.subscriptiq.user.entity.User;
import com.subscriptiq.user.enums.UserRole;
import com.subscriptiq.user.enums.UserStatus;
import com.subscriptiq.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class Phase10IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataCleaner dataCleaner;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UsageRecordRepository usageRecordRepository;

    @Autowired
    private OptimizationInsightRepository optimizationInsightRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String staffToken;
    private String customerToken;

    private User adminUser;
    private Customer testCustomer1;
    private Customer testCustomer2;
    private Plan goldPlan;
    private Plan silverPlan;

    @BeforeEach
    void setUp() {
        dataCleaner.cleanAll();

        // 1. Admin
        adminUser = userRepository.save(User.builder()
                .name("Analytics Admin")
                .email("admin10@test.com")
                .password(passwordEncoder.encode("Admin@1234"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
        adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), "ADMIN");

        // 2. Staff
        User staffUser = userRepository.save(User.builder()
                .name("Analytics Staff")
                .email("staff10@test.com")
                .password(passwordEncoder.encode("Staff@1234"))
                .role(UserRole.STAFF)
                .status(UserStatus.ACTIVE)
                .build());
        staffToken = jwtService.generateToken(staffUser.getId(), staffUser.getEmail(), "STAFF");

        // 3. Customer
        User customerUser = userRepository.save(User.builder()
                .name("Analytics Customer")
                .email("cust10@test.com")
                .password(passwordEncoder.encode("Cust@1234"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());
        customerToken = jwtService.generateToken(customerUser.getId(), customerUser.getEmail(), "CUSTOMER");

        testCustomer1 = customerRepository.save(Customer.builder()
                .companyName("Acme Analytics Corp")
                .contactName("Bob Smith")
                .email("bob10@acme.com")
                .phone("9876543211")
                .status(CustomerStatus.ACTIVE)
                .user(customerUser)
                .build());

        testCustomer2 = customerRepository.save(Customer.builder()
                .companyName("Beta Innovations")
                .contactName("Carol White")
                .email("carol10@beta.com")
                .phone("9876543212")
                .status(CustomerStatus.INACTIVE)
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Cloud Infrastructure")
                .status(ProductStatus.ACTIVE)
                .build());

        ProductVariant variant = productVariantRepository.save(ProductVariant.builder()
                .product(product)
                .name("Dedicated Compute")
                .sku("SKU-COMPUTE")
                .status(VariantStatus.ACTIVE)
                .build());

        goldPlan = planRepository.save(Plan.builder()
                .productVariant(variant)
                .name("Gold Tier")
                .price(new BigDecimal("10000.00"))
                .billingCycle(BillingCycle.MONTHLY)
                .status(PlanStatus.ACTIVE)
                .build());

        silverPlan = planRepository.save(Plan.builder()
                .productVariant(variant)
                .name("Silver Tier")
                .price(new BigDecimal("5000.00"))
                .billingCycle(BillingCycle.MONTHLY)
                .status(PlanStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Scenario 1: Overview - empty database returns zeros without error")
    void scenario1_overview_emptyDatabase_returnsZeros() throws Exception {
        dataCleaner.cleanAll();

        // Recreate only the admin for token authentication
        adminUser = userRepository.save(User.builder()
                .name("Admin")
                .email("admin_clean@test.com")
                .password(passwordEncoder.encode("Admin@1234"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
        adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), "ADMIN");

        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalCustomers", is(0)))
                .andExpect(jsonPath("$.data.activeCustomers", is(0)))
                .andExpect(jsonPath("$.data.totalSubscriptions", is(0)))
                .andExpect(jsonPath("$.data.activeSubscriptions", is(0)))
                .andExpect(jsonPath("$.data.totalInvoices", is(0)))
                .andExpect(jsonPath("$.data.totalBilledAmount", is(0.0)))
                .andExpect(jsonPath("$.data.totalCollectedAmount", is(0.0)))
                .andExpect(jsonPath("$.data.outstandingAmount", is(0.0)));
    }

    @Test
    @DisplayName("Scenario 2: Overview - populated database returns correct aggregates")
    void scenario2_overview_populatedDatabase_returnsCorrectAggregates() throws Exception {
        // 1 active subscription, 1 closed
        Subscription sub1 = subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-AN-1001")
                .customer(testCustomer1)
                .plan(goldPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusMonths(2))
                .endDate(LocalDate.now().plusDays(15))
                .nextBillingDate(LocalDate.now().plusDays(15))
                .quantity(1)
                .unitPrice(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .build());

        Subscription sub2 = subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-AN-1002")
                .customer(testCustomer2)
                .plan(silverPlan)
                .status(SubscriptionStatus.CLOSED)
                .startDate(LocalDate.now().minusMonths(4))
                .endDate(LocalDate.now().minusMonths(1))
                .nextBillingDate(LocalDate.now().minusMonths(1))
                .quantity(1)
                .unitPrice(new BigDecimal("5000.00"))
                .totalAmount(new BigDecimal("5000.00"))
                .build());

        // 1 PAID invoice, 1 OVERDUE invoice
        Invoice inv1 = invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-AN-1001")
                .customer(testCustomer1)
                .subscription(sub1)
                .invoiceDate(LocalDate.now().minusMonths(1))
                .dueDate(LocalDate.now().minusDays(10))
                .billingPeriodStart(LocalDate.now().minusMonths(1))
                .billingPeriodEnd(LocalDate.now())
                .subtotal(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .paidAmount(new BigDecimal("10000.00"))
                .balanceAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.PAID)
                .build());

        Invoice inv2 = invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-AN-1002")
                .customer(testCustomer1)
                .subscription(sub1)
                .invoiceDate(LocalDate.now().minusDays(15))
                .dueDate(LocalDate.now().minusDays(5))
                .billingPeriodStart(LocalDate.now().minusDays(15))
                .billingPeriodEnd(LocalDate.now().plusDays(15))
                .subtotal(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .paidAmount(BigDecimal.ZERO)
                .balanceAmount(new BigDecimal("10000.00"))
                .status(InvoiceStatus.OVERDUE)
                .build());

        // 1 Payment for inv1
        paymentRepository.save(Payment.builder()
                .paymentNumber("PAY-AN-1001")
                .invoice(inv1)
                .customer(testCustomer1)
                .amount(new BigDecimal("10000.00"))
                .paymentDate(LocalDateTime.now().minusDays(10))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.SUCCESS)
                .build());

        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalCustomers", is(2)))
                .andExpect(jsonPath("$.data.activeCustomers", is(1)))
                .andExpect(jsonPath("$.data.totalSubscriptions", is(2)))
                .andExpect(jsonPath("$.data.activeSubscriptions", is(1)))
                .andExpect(jsonPath("$.data.expiringSubscriptions", is(1)))
                .andExpect(jsonPath("$.data.totalInvoices", is(2)))
                .andExpect(jsonPath("$.data.paidInvoices", is(1)))
                .andExpect(jsonPath("$.data.overdueInvoices", is(1)))
                .andExpect(jsonPath("$.data.totalBilledAmount", is(20000.0)))
                .andExpect(jsonPath("$.data.totalCollectedAmount", is(10000.0)))
                .andExpect(jsonPath("$.data.outstandingAmount", is(10000.0)))
                .andExpect(jsonPath("$.data.monthlyRecurringRevenue", is(10000.0)));
    }

    @Test
    @DisplayName("Scenario 3: Revenue - full breakdown by month, plan, customer, and MoM growth")
    void scenario3_revenue_fullBreakdown_and_momGrowth() throws Exception {
        Subscription sub = subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-REV-1")
                .customer(testCustomer1)
                .plan(goldPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .nextBillingDate(LocalDate.of(2026, 4, 1))
                .quantity(1)
                .unitPrice(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .build());

        // Month 1: 10,000 billed, 10,000 paid
        invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-REV-1")
                .customer(testCustomer1)
                .subscription(sub)
                .invoiceDate(LocalDate.of(2026, 1, 10))
                .dueDate(LocalDate.of(2026, 1, 20))
                .billingPeriodStart(LocalDate.of(2026, 1, 1))
                .billingPeriodEnd(LocalDate.of(2026, 1, 31))
                .subtotal(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .paidAmount(new BigDecimal("10000.00"))
                .balanceAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.PAID)
                .build());

        // Month 2: 15,000 billed, 5,000 paid, 10,000 overdue
        invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-REV-2")
                .customer(testCustomer1)
                .subscription(sub)
                .invoiceDate(LocalDate.of(2026, 2, 10))
                .dueDate(LocalDate.of(2026, 2, 20))
                .billingPeriodStart(LocalDate.of(2026, 2, 1))
                .billingPeriodEnd(LocalDate.of(2026, 2, 28))
                .subtotal(new BigDecimal("15000.00"))
                .totalAmount(new BigDecimal("15000.00"))
                .paidAmount(new BigDecimal("5000.00"))
                .balanceAmount(new BigDecimal("10000.00"))
                .status(InvoiceStatus.OVERDUE)
                .build());

        mockMvc.perform(get("/api/analytics/revenue")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalBilledRevenue", is(25000.0)))
                .andExpect(jsonPath("$.data.totalPaidRevenue", is(15000.0)))
                .andExpect(jsonPath("$.data.outstandingRevenue", is(10000.0)))
                .andExpect(jsonPath("$.data.overdueRevenue", is(10000.0)))
                .andExpect(jsonPath("$.data.revenueByMonth", hasSize(2)))
                .andExpect(jsonPath("$.data.revenueByMonth[0].month", is("2026-01")))
                .andExpect(jsonPath("$.data.revenueByMonth[0].billedRevenue", is(10000.0)))
                .andExpect(jsonPath("$.data.revenueByMonth[0].momGrowthPercentage", is(0.0)))
                .andExpect(jsonPath("$.data.revenueByMonth[1].month", is("2026-02")))
                .andExpect(jsonPath("$.data.revenueByMonth[1].billedRevenue", is(15000.0)))
                // (15000 - 10000) / 10000 * 100 = 50.0%
                .andExpect(jsonPath("$.data.revenueByMonth[1].momGrowthPercentage", is(50.0)))
                .andExpect(jsonPath("$.data.revenueByPlan[0].planName", is("Gold Tier")))
                .andExpect(jsonPath("$.data.revenueByCustomer[0].companyName", is("Acme Analytics Corp")));
    }

    @Test
    @DisplayName("Scenario 4: Revenue - date filtering and planId filtering")
    void scenario4_revenue_dateFiltering_and_planFilter() throws Exception {
        Subscription goldSub = subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-REV-G")
                .customer(testCustomer1)
                .plan(goldPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .nextBillingDate(LocalDate.of(2026, 4, 1))
                .quantity(1)
                .unitPrice(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .build());

        Subscription silverSub = subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-REV-S")
                .customer(testCustomer1)
                .plan(silverPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .nextBillingDate(LocalDate.of(2026, 4, 1))
                .quantity(1)
                .unitPrice(new BigDecimal("5000.00"))
                .totalAmount(new BigDecimal("5000.00"))
                .build());

        // January (Gold)
        invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-F-1")
                .customer(testCustomer1)
                .subscription(goldSub)
                .invoiceDate(LocalDate.of(2026, 1, 15))
                .dueDate(LocalDate.of(2026, 1, 25))
                .billingPeriodStart(LocalDate.of(2026, 1, 1))
                .billingPeriodEnd(LocalDate.of(2026, 1, 31))
                .subtotal(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .paidAmount(new BigDecimal("10000.00"))
                .balanceAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.PAID)
                .build());

        // February (Silver)
        invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-F-2")
                .customer(testCustomer1)
                .subscription(silverSub)
                .invoiceDate(LocalDate.of(2026, 2, 15))
                .dueDate(LocalDate.of(2026, 2, 25))
                .billingPeriodStart(LocalDate.of(2026, 2, 1))
                .billingPeriodEnd(LocalDate.of(2026, 2, 28))
                .subtotal(new BigDecimal("5000.00"))
                .totalAmount(new BigDecimal("5000.00"))
                .paidAmount(new BigDecimal("5000.00"))
                .balanceAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.PAID)
                .build());

        // Filter by planId = goldPlan.getId()
        mockMvc.perform(get("/api/analytics/revenue")
                        .param("planId", goldPlan.getId().toString())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalBilledRevenue", is(10000.0)))
                .andExpect(jsonPath("$.data.revenueByPlan", hasSize(1)))
                .andExpect(jsonPath("$.data.revenueByPlan[0].planName", is("Gold Tier")));

        // Filter by date range: only February
        mockMvc.perform(get("/api/analytics/revenue")
                        .param("startDate", "2026-02-01")
                        .param("endDate", "2026-02-28")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalBilledRevenue", is(5000.0)))
                .andExpect(jsonPath("$.data.revenueByMonth", hasSize(1)))
                .andExpect(jsonPath("$.data.revenueByMonth[0].month", is("2026-02")));
    }

    @Test
    @DisplayName("Scenario 5: Revenue - invalid date range (startDate > endDate) rejected with HTTP 400")
    void scenario5_revenue_invalidDateRange_rejected() throws Exception {
        mockMvc.perform(get("/api/analytics/revenue")
                        .param("startDate", "2026-05-01")
                        .param("endDate", "2026-04-01")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("startDate must not be after endDate")));
    }

    @Test
    @DisplayName("Scenario 6: Subscriptions - status counts and monthly acquisition")
    void scenario6_subscriptions_statusCounts_and_monthlyAcquisition() throws Exception {
        subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-S-1")
                .customer(testCustomer1)
                .plan(goldPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 5))
                .nextBillingDate(LocalDate.of(2026, 2, 5))
                .quantity(1)
                .unitPrice(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .build());

        subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-S-2")
                .customer(testCustomer1)
                .plan(goldPlan)
                .status(SubscriptionStatus.PAUSED)
                .startDate(LocalDate.of(2026, 1, 20))
                .nextBillingDate(LocalDate.of(2026, 2, 20))
                .quantity(1)
                .unitPrice(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .build());

        subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-S-3")
                .customer(testCustomer2)
                .plan(silverPlan)
                .status(SubscriptionStatus.EXPIRED)
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(LocalDate.of(2026, 3, 1))
                .nextBillingDate(LocalDate.of(2026, 3, 1))
                .quantity(1)
                .unitPrice(new BigDecimal("5000.00"))
                .totalAmount(new BigDecimal("5000.00"))
                .build());

        mockMvc.perform(get("/api/analytics/subscriptions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalSubscriptions", is(3)))
                .andExpect(jsonPath("$.data.activeSubscriptions", is(1)))
                .andExpect(jsonPath("$.data.pausedSubscriptions", is(1)))
                .andExpect(jsonPath("$.data.expiredSubscriptions", is(1)))
                .andExpect(jsonPath("$.data.newSubscriptionsByMonth", hasSize(2)))
                .andExpect(jsonPath("$.data.subscriptionsByPlan", hasSize(2)))
                .andExpect(jsonPath("$.data.subscriptionsByBillingCycle.MONTHLY", is(3)));
    }

    @Test
    @DisplayName("Scenario 7: Customers - counts, plan distribution, and average subscriptions per customer")
    void scenario7_customers_counts_and_planDistribution() throws Exception {
        subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-C-1")
                .customer(testCustomer1)
                .plan(goldPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusMonths(1))
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .quantity(1)
                .unitPrice(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .build());

        subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-C-2")
                .customer(testCustomer1)
                .plan(silverPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusMonths(1))
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .quantity(1)
                .unitPrice(new BigDecimal("5000.00"))
                .totalAmount(new BigDecimal("5000.00"))
                .build());

        mockMvc.perform(get("/api/analytics/customers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalCustomers", is(2)))
                .andExpect(jsonPath("$.data.activeCustomers", is(1)))
                .andExpect(jsonPath("$.data.inactiveCustomers", is(1)))
                .andExpect(jsonPath("$.data.averageSubscriptionsPerCustomer", is(1.0)));
    }

    @Test
    @DisplayName("Scenario 8: Payments - collection metrics, success rate, and monthly breakdown")
    void scenario8_payments_collectionMetrics_and_successRate() throws Exception {
        Invoice inv = invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-PAY-1")
                .customer(testCustomer1)
                .invoiceDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().plusDays(10))
                .subtotal(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .paidAmount(new BigDecimal("6000.00"))
                .balanceAmount(new BigDecimal("4000.00"))
                .status(InvoiceStatus.PARTIALLY_PAID)
                .build());

        paymentRepository.save(Payment.builder()
                .paymentNumber("PAY-S-1")
                .invoice(inv)
                .customer(testCustomer1)
                .amount(new BigDecimal("6000.00"))
                .paymentDate(LocalDateTime.now().minusDays(5))
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.SUCCESS)
                .build());

        paymentRepository.save(Payment.builder()
                .paymentNumber("PAY-F-1")
                .invoice(inv)
                .customer(testCustomer1)
                .amount(new BigDecimal("4000.00"))
                .paymentDate(LocalDateTime.now().minusDays(4))
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.FAILED)
                .build());

        mockMvc.perform(get("/api/analytics/payments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalPayments", is(2)))
                .andExpect(jsonPath("$.data.totalCollected", is(6000.0)))
                .andExpect(jsonPath("$.data.successfulPayments", is(1)))
                .andExpect(jsonPath("$.data.failedPayments", is(1)))
                // 1 success out of 2 = 50.0%
                .andExpect(jsonPath("$.data.paymentSuccessRate", is(50.0)))
                .andExpect(jsonPath("$.data.monthlyPaymentCollection", hasSize(1)));
    }

    @Test
    @DisplayName("Scenario 9: Churn - deterministic calculations and zero-denominator handling")
    void scenario9_churn_deterministicCalculations_and_zeroDenominator() throws Exception {
        // Active subscription at start (started 6 months ago, still active)
        subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-CHURN-1")
                .customer(testCustomer1)
                .plan(goldPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusMonths(6))
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .quantity(1)
                .unitPrice(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .build());

        // Closed subscription (churned 1 month ago)
        subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-CHURN-2")
                .customer(testCustomer2)
                .plan(goldPlan)
                .status(SubscriptionStatus.CLOSED)
                .startDate(LocalDate.now().minusMonths(6))
                .endDate(LocalDate.now().minusMonths(1))
                .nextBillingDate(LocalDate.now().minusMonths(1))
                .quantity(1)
                .unitPrice(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .build());

        mockMvc.perform(get("/api/analytics/churn")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.closedSubscriptions", is(1)))
                .andExpect(jsonPath("$.data.totalChurned", is(1)))
                .andExpect(jsonPath("$.data.methodology", is("HISTORICAL_SUBSCRIPTION_LIFECYCLE")))
                .andExpect(jsonPath("$.data.churnByPlan", hasSize(2)));
    }

    @Test
    @DisplayName("Scenario 10: Forecast - recent monthly average produces deterministic projections")
    void scenario10_forecast_recentMonthlyAverage() throws Exception {
        Subscription sub = subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-FC-1")
                .customer(testCustomer1)
                .plan(goldPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .nextBillingDate(LocalDate.of(2026, 4, 1))
                .quantity(1)
                .unitPrice(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .build());

        invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-FC-1")
                .customer(testCustomer1)
                .subscription(sub)
                .invoiceDate(LocalDate.of(2026, 1, 15))
                .dueDate(LocalDate.of(2026, 1, 25))
                .subtotal(new BigDecimal("20000.00"))
                .totalAmount(new BigDecimal("20000.00"))
                .paidAmount(new BigDecimal("20000.00"))
                .balanceAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.PAID)
                .build());

        invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-FC-2")
                .customer(testCustomer1)
                .subscription(sub)
                .invoiceDate(LocalDate.of(2026, 2, 15))
                .dueDate(LocalDate.of(2026, 2, 25))
                .subtotal(new BigDecimal("40000.00"))
                .totalAmount(new BigDecimal("40000.00"))
                .paidAmount(new BigDecimal("40000.00"))
                .balanceAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.PAID)
                .build());

        // Average of 20,000 and 40,000 is 30,000.00
        mockMvc.perform(get("/api/analytics/forecast")
                        .param("months", "3")
                        .param("methodology", "RECENT_MONTHLY_AVERAGE")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.isSufficientData", is(true)))
                .andExpect(jsonPath("$.data.confidence", is("BASELINE")))
                .andExpect(jsonPath("$.data.historicalPeriodMonths", is(2)))
                .andExpect(jsonPath("$.data.averageMonthlyRevenue", is(30000.0)))
                .andExpect(jsonPath("$.data.forecast", hasSize(3)))
                .andExpect(jsonPath("$.data.forecast[0].projectedRevenue", is(30000.0)));
    }

    @Test
    @DisplayName("Scenario 11: Forecast - weighted recent average prioritizes recent revenue")
    void scenario11_forecast_weightedRecentAverage() throws Exception {
        Subscription sub = subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-WFC-1")
                .customer(testCustomer1)
                .plan(goldPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .nextBillingDate(LocalDate.of(2026, 4, 1))
                .quantity(1)
                .unitPrice(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .build());

        // Month 1 (weight 1): 10,000
        invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-WFC-1")
                .customer(testCustomer1)
                .subscription(sub)
                .invoiceDate(LocalDate.of(2026, 1, 15))
                .dueDate(LocalDate.of(2026, 1, 25))
                .subtotal(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .paidAmount(new BigDecimal("10000.00"))
                .balanceAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.PAID)
                .build());

        // Month 2 (weight 2): 40,000
        invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-WFC-2")
                .customer(testCustomer1)
                .subscription(sub)
                .invoiceDate(LocalDate.of(2026, 2, 15))
                .dueDate(LocalDate.of(2026, 2, 25))
                .subtotal(new BigDecimal("40000.00"))
                .totalAmount(new BigDecimal("40000.00"))
                .paidAmount(new BigDecimal("40000.00"))
                .balanceAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.PAID)
                .build());

        // Weighted average = (10,000*1 + 40,000*2) / 3 = 90,000 / 3 = 30,000.00
        mockMvc.perform(get("/api/analytics/forecast")
                        .param("months", "2")
                        .param("methodology", "WEIGHTED_RECENT_AVERAGE")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.methodology", is("WEIGHTED_RECENT_AVERAGE")))
                .andExpect(jsonPath("$.data.averageMonthlyRevenue", is(30000.0)))
                .andExpect(jsonPath("$.data.forecast", hasSize(2)));
    }

    @Test
    @DisplayName("Scenario 12: Forecast - insufficient historical data handled gracefully")
    void scenario12_forecast_insufficientData_handledGracefully() throws Exception {
        mockMvc.perform(get("/api/analytics/forecast")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isSufficientData", is(false)))
                .andExpect(jsonPath("$.data.confidence", is("INSUFFICIENT_DATA")))
                .andExpect(jsonPath("$.data.forecast", hasSize(0)));
    }

    @Test
    @DisplayName("Scenario 13: Plans - performance metrics per plan")
    void scenario13_plans_performanceMetrics() throws Exception {
        Subscription goldSub = subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-P-G")
                .customer(testCustomer1)
                .plan(goldPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusMonths(1))
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .quantity(1)
                .unitPrice(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .build());

        invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-P-1")
                .customer(testCustomer1)
                .subscription(goldSub)
                .invoiceDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().plusDays(10))
                .subtotal(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .paidAmount(new BigDecimal("10000.00"))
                .balanceAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.PAID)
                .build());

        mockMvc.perform(get("/api/analytics/plans")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].planName", notNullValue()))
                .andExpect(jsonPath("$.data[0].totalRevenue", notNullValue()));
    }

    @Test
    @DisplayName("Scenario 14: Optimization - returns unused resources and potential savings read-only")
    void scenario14_optimization_analytics_readOnly() throws Exception {
        Subscription sub = subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-OPT-1")
                .customer(testCustomer1)
                .plan(goldPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusMonths(1))
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .quantity(1)
                .unitPrice(new BigDecimal("10000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .build());

        UsageRecord usage = usageRecordRepository.save(UsageRecord.builder()
                .customer(testCustomer1)
                .subscription(sub)
                .resourceName("Compute Core")
                .resourceType("CPU")
                .allocatedQuantity(10)
                .usedQuantity(2)
                .unitCost(new BigDecimal("100.00"))
                .snapshotDate(LocalDate.now().minusDays(5))
                .build());

        optimizationInsightRepository.save(OptimizationInsight.builder()
                .customer(testCustomer1)
                .subscription(sub)
                .usageRecord(usage)
                .resourceName("Compute Core")
                .allocatedQuantity(10)
                .usedQuantity(2)
                .unusedQuantity(8)
                .utilizationPercentage(new BigDecimal("20.00"))
                .unitCost(new BigDecimal("100.00"))
                .potentialMonthlySavings(new BigDecimal("800.00"))
                .potentialAnnualSavings(new BigDecimal("9600.00"))
                .recommendation(OptimizationRecommendation.REDUCE_LICENSES)
                .status(OptimizationStatus.DETECTED)
                .build());

        mockMvc.perform(get("/api/analytics/optimization")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.unusedResources", is(8)))
                .andExpect(jsonPath("$.data.potentialMonthlySavings", is(800.0)))
                .andExpect(jsonPath("$.data.potentialAnnualSavings", is(9600.0)))
                .andExpect(jsonPath("$.data.activeOpportunitiesCount", is(1)))
                .andExpect(jsonPath("$.data.opportunities", hasSize(1)))
                .andExpect(jsonPath("$.data.opportunities[0].resourceName", is("Compute Core")))
                .andExpect(jsonPath("$.data.utilizationSummary.underutilizedCount", is(1)));
    }

    @Test
    @DisplayName("Scenario 15: Dashboard - consolidated summary provides all modules")
    void scenario15_dashboard_consolidatedSummary() throws Exception {
        mockMvc.perform(get("/api/analytics/dashboard")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.revenue", notNullValue()))
                .andExpect(jsonPath("$.data.subscriptions", notNullValue()))
                .andExpect(jsonPath("$.data.customers", notNullValue()))
                .andExpect(jsonPath("$.data.payments", notNullValue()))
                .andExpect(jsonPath("$.data.churn", notNullValue()))
                .andExpect(jsonPath("$.data.optimization", notNullValue()));
    }

    @Test
    @DisplayName("Scenario 16: Security RBAC - Admin succeeds, Staff forbidden, Customer forbidden, Unauthenticated unauthorized")
    void scenario16_security_rbac() throws Exception {
        // 1. Admin succeeds (HTTP 200)
        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 2. Staff is forbidden (HTTP 403)
        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/analytics/revenue")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());

        // 3. Customer is forbidden (HTTP 403)
        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/analytics/forecast")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        // 4. Unauthenticated is unauthorized (HTTP 401)
        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/analytics/revenue"))
                .andExpect(status().isUnauthorized());
    }
}

package com.subscriptiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscriptiq.config.BillingProperties;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.enums.CustomerStatus;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import com.subscriptiq.payment.dto.CreatePaymentRequest;
import com.subscriptiq.payment.entity.Payment;
import com.subscriptiq.payment.enums.PaymentMethod;
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
import com.subscriptiq.quotation.repository.QuotationRepository;
import com.subscriptiq.scheduler.dto.BillingRunSummary;
import com.subscriptiq.scheduler.service.RecurringBillingService;
import com.subscriptiq.scheduler.service.SubscriptionBillingProcessor;
import com.subscriptiq.security.JwtService;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class Phase6IntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

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
        private QuotationRepository quotationRepository;

        @Autowired
        private SubscriptionRepository subscriptionRepository;

        @Autowired
        private InvoiceRepository invoiceRepository;

        @Autowired
        private PaymentRepository paymentRepository;

        @Autowired
        private RecurringBillingService recurringBillingService;

        @Autowired
        private SubscriptionBillingProcessor billingProcessor;

        @Autowired
        private BillingProperties billingProperties;

        @Autowired
        private JwtService jwtService;

        @Autowired
        private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDataCleaner dataCleaner;

        private String adminToken;
        private String staffToken;
        private String customerToken;

        private Customer customer;
        private Plan monthlyPlan;
        private Plan quarterlyPlan;
        private Plan yearlyPlan;

    @BeforeEach
    void setUp() {
        dataCleaner.cleanAll();

                // 1. Users & Tokens
                User adminUser = userRepository.save(User.builder()
                                .name("Admin User")
                                .email("admin@test.com")
                                .password(passwordEncoder.encode("Password@123"))
                                .role(UserRole.ADMIN)
                                .status(UserStatus.ACTIVE)
                                .build());
                adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), "ADMIN");

                User staffUser = userRepository.save(User.builder()
                                .name("Staff User")
                                .email("staff@test.com")
                                .password(passwordEncoder.encode("Password@123"))
                                .role(UserRole.STAFF)
                                .status(UserStatus.ACTIVE)
                                .build());
                staffToken = jwtService.generateToken(staffUser.getId(), staffUser.getEmail(), "STAFF");

                User customerUser = userRepository.save(User.builder()
                                .name("Customer User")
                                .email("customer@test.com")
                                .password(passwordEncoder.encode("Password@123"))
                                .role(UserRole.CUSTOMER)
                                .status(UserStatus.ACTIVE)
                                .build());
                customerToken = jwtService.generateToken(customerUser.getId(), customerUser.getEmail(), "CUSTOMER");

                customer = customerRepository.save(Customer.builder()
                                .companyName("CloudWorks Inc")
                                .contactName("Charlie")
                                .email("charlie@cloudworks.com")
                                .phone("+1555123456")
                                .status(CustomerStatus.ACTIVE)
                                .user(customerUser)
                                .build());

                // 2. Products, Variants & Plans
                Product product = productRepository.save(Product.builder()
                                .name("Cloud Billing Platform")
                                .description("Automated Billing Suite")
                                .category("FINTECH")
                                .status(ProductStatus.ACTIVE)
                                .build());

                ProductVariant variant = productVariantRepository.save(ProductVariant.builder()
                                .product(product)
                                .name("Standard Suite")
                                .sku("CBP-STD")
                                .status(VariantStatus.ACTIVE)
                                .build());

                monthlyPlan = planRepository.save(Plan.builder()
                                .productVariant(variant)
                                .name("Monthly Pro")
                                .billingCycle(BillingCycle.MONTHLY)
                                .price(new BigDecimal("200.00"))
                                .status(PlanStatus.ACTIVE)
                                .build());

                quarterlyPlan = planRepository.save(Plan.builder()
                                .productVariant(variant)
                                .name("Quarterly Pro")
                                .billingCycle(BillingCycle.QUARTERLY)
                                .price(new BigDecimal("550.00"))
                                .status(PlanStatus.ACTIVE)
                                .build());

                yearlyPlan = planRepository.save(Plan.builder()
                                .productVariant(variant)
                                .name("Yearly Enterprise")
                                .billingCycle(BillingCycle.YEARLY)
                                .price(new BigDecimal("2000.00"))
                                .status(PlanStatus.ACTIVE)
                                .build());
        }

        // -------------------------------------------------------------
        // Scenario 1: Eligible monthly subscription generates invoice
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 1: Eligible monthly subscription generates invoice and advances nextBillingDate")
        void scenario1_eligibleMonthlySubscriptionGeneratesInvoice() {
                LocalDate billingDate = LocalDate.now();

                Subscription subscription = subscriptionRepository.save(Subscription.builder()
                                .subscriptionNumber("SUB-M-001")
                                .customer(customer)
                                .plan(monthlyPlan)
                                .quantity(2)
                                .unitPrice(new BigDecimal("200.00"))
                                .discountAmount(new BigDecimal("20.00"))
                                .taxAmount(new BigDecimal("38.00"))
                                .totalAmount(new BigDecimal("418.00"))
                                .status(SubscriptionStatus.ACTIVE)
                                .startDate(billingDate.minusMonths(1))
                                .nextBillingDate(billingDate)
                                .build());

                BillingRunSummary summary = recurringBillingService.runRecurringBilling(billingDate);

                assertThat(summary.getSubscriptionsChecked()).isEqualTo(1);
                assertThat(summary.getInvoicesCreated()).isEqualTo(1);
                assertThat(summary.getAlreadyProcessed()).isEqualTo(0);

                List<Invoice> invoices = invoiceRepository.findBySubscriptionId(subscription.getId());
                assertThat(invoices).hasSize(1);
                Invoice invoice = invoices.get(0);

                assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
                assertThat(invoice.getInvoiceDate()).isEqualTo(billingDate);
                assertThat(invoice.getBillingPeriodStart()).isEqualTo(billingDate);
                assertThat(invoice.getBillingPeriodEnd()).isEqualTo(billingDate.plusMonths(1));
                assertThat(invoice.getSubtotal()).isEqualByComparingTo(new BigDecimal("400.00"));
                assertThat(invoice.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("20.00"));
                assertThat(invoice.getTaxAmount()).isEqualByComparingTo(new BigDecimal("38.00"));
                assertThat(invoice.getTotalAmount()).isEqualByComparingTo(new BigDecimal("418.00"));
                assertThat(invoice.getBalanceAmount()).isEqualByComparingTo(new BigDecimal("418.00"));

                Subscription updated = subscriptionRepository.findById(subscription.getId()).orElseThrow();
                assertThat(updated.getNextBillingDate()).isEqualTo(billingDate.plusMonths(1));
        }

        // -------------------------------------------------------------
        // Scenario 2: Quarterly subscription calculates next billing date correctly
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 2: Quarterly subscription calculates next billing date using plusMonths(3)")
        void scenario2_quarterlySubscriptionAdvancesThreeMonths() {
                LocalDate billingDate = LocalDate.now();

                Subscription subscription = subscriptionRepository.save(Subscription.builder()
                                .subscriptionNumber("SUB-Q-001")
                                .customer(customer)
                                .plan(quarterlyPlan)
                                .quantity(1)
                                .unitPrice(new BigDecimal("550.00"))
                                .status(SubscriptionStatus.ACTIVE)
                                .startDate(billingDate.minusMonths(3))
                                .nextBillingDate(billingDate)
                                .build());

                recurringBillingService.runRecurringBilling(billingDate);

                Subscription updated = subscriptionRepository.findById(subscription.getId()).orElseThrow();
                assertThat(updated.getNextBillingDate()).isEqualTo(billingDate.plusMonths(3));
        }

        // -------------------------------------------------------------
        // Scenario 3: Yearly subscription calculates next billing date correctly
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 3: Yearly subscription calculates next billing date using plusYears(1)")
        void scenario3_yearlySubscriptionAdvancesOneYear() {
                LocalDate billingDate = LocalDate.now();

                Subscription subscription = subscriptionRepository.save(Subscription.builder()
                                .subscriptionNumber("SUB-Y-001")
                                .customer(customer)
                                .plan(yearlyPlan)
                                .quantity(1)
                                .unitPrice(new BigDecimal("2000.00"))
                                .status(SubscriptionStatus.ACTIVE)
                                .startDate(billingDate.minusYears(1))
                                .nextBillingDate(billingDate)
                                .build());

                recurringBillingService.runRecurringBilling(billingDate);

                Subscription updated = subscriptionRepository.findById(subscription.getId()).orElseThrow();
                assertThat(updated.getNextBillingDate()).isEqualTo(billingDate.plusYears(1));
        }

        // -------------------------------------------------------------
        // Scenario 4: Non-active subscription is skipped
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 4: Non-active subscriptions (PAUSED, CLOSED) are skipped")
        void scenario4_nonActiveSubscriptionSkipped() {
                LocalDate billingDate = LocalDate.now();

                subscriptionRepository.save(Subscription.builder()
                                .subscriptionNumber("SUB-PAUSED")
                                .customer(customer)
                                .plan(monthlyPlan)
                                .quantity(1)
                                .unitPrice(new BigDecimal("200.00"))
                                .status(SubscriptionStatus.PAUSED)
                                .startDate(billingDate.minusMonths(1))
                                .nextBillingDate(billingDate)
                                .build());

                BillingRunSummary summary = recurringBillingService.runRecurringBilling(billingDate);

                assertThat(summary.getSubscriptionsChecked()).isEqualTo(0);
                assertThat(summary.getInvoicesCreated()).isEqualTo(0);
        }

        // -------------------------------------------------------------
        // Scenario 5: Future nextBillingDate is skipped
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 5: Subscription with future nextBillingDate is not billed yet")
        void scenario5_futureBillingDateSkipped() {
                LocalDate today = LocalDate.now();

                subscriptionRepository.save(Subscription.builder()
                                .subscriptionNumber("SUB-FUTURE")
                                .customer(customer)
                                .plan(monthlyPlan)
                                .quantity(1)
                                .unitPrice(new BigDecimal("200.00"))
                                .status(SubscriptionStatus.ACTIVE)
                                .startDate(today)
                                .nextBillingDate(today.plusDays(10))
                                .build());

                BillingRunSummary summary = recurringBillingService.runRecurringBilling(today);

                assertThat(summary.getSubscriptionsChecked()).isEqualTo(0);
                assertThat(summary.getInvoicesCreated()).isEqualTo(0);
        }

        // -------------------------------------------------------------
        // Scenario 6: Duplicate billing does not create duplicate invoice
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 6: Running billing twice does not create duplicate invoice")
        void scenario6_duplicateBillingDoesNotCreateDuplicateInvoice() {
                LocalDate billingDate = LocalDate.now();

                Subscription subscription = subscriptionRepository.save(Subscription.builder()
                                .subscriptionNumber("SUB-DUP-001")
                                .customer(customer)
                                .plan(monthlyPlan)
                                .quantity(1)
                                .unitPrice(new BigDecimal("200.00"))
                                .status(SubscriptionStatus.ACTIVE)
                                .startDate(billingDate.minusMonths(1))
                                .nextBillingDate(billingDate)
                                .build());

                // First run: Creates invoice
                BillingRunSummary run1 = recurringBillingService.runRecurringBilling(billingDate);
                assertThat(run1.getInvoicesCreated()).isEqualTo(1);

                // Manually reset nextBillingDate back to billingDate to simulate duplicate
                // check on same period
                subscription.setNextBillingDate(billingDate);
                subscriptionRepository.saveAndFlush(subscription);

                // Second run: Duplicate detected and skipped
                BillingRunSummary run2 = recurringBillingService.runRecurringBilling(billingDate);
                assertThat(run2.getInvoicesCreated()).isEqualTo(0);
                assertThat(run2.getAlreadyProcessed()).isEqualTo(1);

                List<Invoice> invoices = invoiceRepository.findBySubscriptionId(subscription.getId());
                assertThat(invoices).hasSize(1);
        }

        // -------------------------------------------------------------
        // Scenario 7: Manual billing endpoint works for ADMIN
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 7: ADMIN can trigger manual billing via POST /api/admin/billing/run")
        void scenario7_adminCanTriggerManualBilling() throws Exception {
                LocalDate billingDate = LocalDate.now();

                subscriptionRepository.save(Subscription.builder()
                                .subscriptionNumber("SUB-ADMIN-TRIGGER")
                                .customer(customer)
                                .plan(monthlyPlan)
                                .quantity(1)
                                .unitPrice(new BigDecimal("200.00"))
                                .status(SubscriptionStatus.ACTIVE)
                                .startDate(billingDate.minusMonths(1))
                                .nextBillingDate(billingDate)
                                .build());

                mockMvc.perform(post("/api/admin/billing/run")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.data.subscriptionsChecked", is(1)))
                                .andExpect(jsonPath("$.data.invoicesCreated", is(1)))
                                .andExpect(jsonPath("$.data.failed", is(0)));
        }

        // -------------------------------------------------------------
        // Scenario 8: CUSTOMER cannot trigger billing (403 Forbidden)
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 8: CUSTOMER is forbidden from triggering manual billing (403)")
        void scenario8_customerCannotTriggerBilling() throws Exception {
                mockMvc.perform(post("/api/admin/billing/run")
                                .header("Authorization", "Bearer " + customerToken))
                                .andExpect(status().isForbidden());
        }

        // -------------------------------------------------------------
        // Scenario 9: STAFF cannot trigger admin billing (403 Forbidden)
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 9: STAFF is forbidden from triggering admin billing (403)")
        void scenario9_staffCannotTriggerBilling() throws Exception {
                mockMvc.perform(post("/api/admin/billing/run")
                                .header("Authorization", "Bearer " + staffToken))
                                .andExpect(status().isForbidden());
        }

        // -------------------------------------------------------------
        // Scenario 10: Invoice due date is calculated correctly (invoiceDate + dueDays)
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 10: Invoice due date is configured/default payment terms (invoiceDate + dueDays)")
        void scenario10_invoiceDueDateCalculatedCorrectly() {
                LocalDate today = LocalDate.now();
                int configuredDueDays = billingProperties.getInvoice().getDueDays();

                Subscription subscription = subscriptionRepository.save(Subscription.builder()
                                .subscriptionNumber("SUB-DUE-001")
                                .customer(customer)
                                .plan(monthlyPlan)
                                .quantity(1)
                                .unitPrice(new BigDecimal("200.00"))
                                .status(SubscriptionStatus.ACTIVE)
                                .startDate(today.minusMonths(1))
                                .nextBillingDate(today)
                                .build());

                recurringBillingService.runRecurringBilling(today);

                List<Invoice> invoices = invoiceRepository.findBySubscriptionId(subscription.getId());
                assertThat(invoices).hasSize(1);
                Invoice invoice = invoices.get(0);

                assertThat(invoice.getDueDate()).isEqualTo(today.plusDays(configuredDueDays));
        }

        // -------------------------------------------------------------
        // Scenario 11: Overdue invoice becomes OVERDUE
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 11: ISSUED invoice past due date with balance > 0 becomes OVERDUE")
        void scenario11_issuedInvoicePastDueDateBecomesOverdue() {
                Invoice invoice = invoiceRepository.save(Invoice.builder()
                                .invoiceNumber("INV-DUE-001")
                                .customer(customer)
                                .invoiceDate(LocalDate.now().minusDays(20))
                                .dueDate(LocalDate.now().minusDays(5))
                                .status(InvoiceStatus.ISSUED)
                                .subtotal(new BigDecimal("300.00"))
                                .totalAmount(new BigDecimal("300.00"))
                                .paidAmount(BigDecimal.ZERO)
                                .balanceAmount(new BigDecimal("300.00"))
                                .items(Collections.emptyList())
                                .build());

                BillingRunSummary summary = recurringBillingService.runRecurringBilling(LocalDate.now());

                assertThat(summary.getOverdueInvoicesUpdated()).isGreaterThanOrEqualTo(1);

                Invoice updated = invoiceRepository.findById(invoice.getId()).orElseThrow();
                assertThat(updated.getStatus()).isEqualTo(InvoiceStatus.OVERDUE);
        }

        // -------------------------------------------------------------
        // Scenario 12: PAID invoice does not become OVERDUE
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 12: Fully PAID invoice past due date is not marked OVERDUE")
        void scenario12_paidInvoiceNotMarkedOverdue() {
                Invoice invoice = invoiceRepository.save(Invoice.builder()
                                .invoiceNumber("INV-PAID-001")
                                .customer(customer)
                                .invoiceDate(LocalDate.now().minusDays(20))
                                .dueDate(LocalDate.now().minusDays(5))
                                .status(InvoiceStatus.PAID)
                                .subtotal(new BigDecimal("300.00"))
                                .totalAmount(new BigDecimal("300.00"))
                                .paidAmount(new BigDecimal("300.00"))
                                .balanceAmount(BigDecimal.ZERO)
                                .items(Collections.emptyList())
                                .build());

                recurringBillingService.runRecurringBilling(LocalDate.now());

                Invoice updated = invoiceRepository.findById(invoice.getId()).orElseThrow();
                assertThat(updated.getStatus()).isEqualTo(InvoiceStatus.PAID);
        }

        // -------------------------------------------------------------
        // Scenario 13: CANCELLED invoice does not become OVERDUE
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 13: CANCELLED invoice past due date is not marked OVERDUE")
        void scenario13_cancelledInvoiceNotMarkedOverdue() {
                Invoice invoice = invoiceRepository.save(Invoice.builder()
                                .invoiceNumber("INV-CANCEL-001")
                                .customer(customer)
                                .invoiceDate(LocalDate.now().minusDays(20))
                                .dueDate(LocalDate.now().minusDays(5))
                                .status(InvoiceStatus.CANCELLED)
                                .subtotal(new BigDecimal("300.00"))
                                .totalAmount(new BigDecimal("300.00"))
                                .paidAmount(BigDecimal.ZERO)
                                .balanceAmount(new BigDecimal("300.00"))
                                .items(Collections.emptyList())
                                .build());

                recurringBillingService.runRecurringBilling(LocalDate.now());

                Invoice updated = invoiceRepository.findById(invoice.getId()).orElseThrow();
                assertThat(updated.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
        }

        // -------------------------------------------------------------
        // Scenario 14: PARTIALLY_PAID overdue invoice becomes OVERDUE
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 14: PARTIALLY_PAID invoice past due date with remaining balance becomes OVERDUE")
        void scenario14_partiallyPaidInvoiceBecomesOverdue() {
                Invoice invoice = invoiceRepository.save(Invoice.builder()
                                .invoiceNumber("INV-PARTIAL-001")
                                .customer(customer)
                                .invoiceDate(LocalDate.now().minusDays(20))
                                .dueDate(LocalDate.now().minusDays(3))
                                .status(InvoiceStatus.PARTIALLY_PAID)
                                .subtotal(new BigDecimal("300.00"))
                                .totalAmount(new BigDecimal("300.00"))
                                .paidAmount(new BigDecimal("100.00"))
                                .balanceAmount(new BigDecimal("200.00"))
                                .items(Collections.emptyList())
                                .build());

                recurringBillingService.runRecurringBilling(LocalDate.now());

                Invoice updated = invoiceRepository.findById(invoice.getId()).orElseThrow();
                assertThat(updated.getStatus()).isEqualTo(InvoiceStatus.OVERDUE);
        }

        // -------------------------------------------------------------
        // Scenario 15: Billing transaction rolls back on failure
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 15: Billing processor maintains transactional boundary; failed item rolls back")
        void scenario15_transactionRollsBackOnFailure() {
                LocalDate today = LocalDate.now();

                // Subscription without valid customer or invalid id
                Long nonExistentId = 999999L;
                assertThatThrownBy(() -> billingProcessor.processSingleSubscription(nonExistentId, today))
                                .isInstanceOf(Exception.class);

                // Verify no orphaned invoices were saved
                List<Invoice> invoices = invoiceRepository.findAll();
                assertThat(invoices).isEmpty();
        }

        // -------------------------------------------------------------
        // Scenario 16: Existing Phase 5 payment flow still works
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 16: Existing Phase 5 payment flow records payments on generated recurring invoices")
        void scenario16_paymentFlowOnRecurringInvoice() throws Exception {
                LocalDate today = LocalDate.now();

                Subscription subscription = subscriptionRepository.save(Subscription.builder()
                                .subscriptionNumber("SUB-PAY-FLOW")
                                .customer(customer)
                                .plan(monthlyPlan)
                                .quantity(1)
                                .unitPrice(new BigDecimal("200.00"))
                                .status(SubscriptionStatus.ACTIVE)
                                .startDate(today.minusMonths(1))
                                .nextBillingDate(today)
                                .build());

                recurringBillingService.runRecurringBilling(today);

                List<Invoice> invoices = invoiceRepository.findBySubscriptionId(subscription.getId());
                assertThat(invoices).hasSize(1);
                Invoice invoice = invoices.get(0);

                // Record full payment using existing Phase 5 API
                CreatePaymentRequest payReq = CreatePaymentRequest.builder()
                                .amount(invoice.getTotalAmount())
                                .paymentMethod(PaymentMethod.CARD)
                                .transactionReference("REC-TXN-999")
                                .notes("Payment for recurring invoice")
                                .build();

                mockMvc.perform(post("/api/invoices/" + invoice.getId() + "/payments")
                                .header("Authorization", "Bearer " + staffToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payReq)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.status", is("SUCCESS")));

                Invoice paidInvoice = invoiceRepository.findById(invoice.getId()).orElseThrow();
                assertThat(paidInvoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
                assertThat(paidInvoice.getBalanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        // -------------------------------------------------------------
        // Scenario 17: Scheduler can be disabled in development/tests
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 17: Scheduler can be dynamically enabled/disabled via configuration")
        void scenario17_schedulerCanBeDisabled() {
                billingProperties.getScheduler().setEnabled(false);
                assertThat(billingProperties.getScheduler().isEnabled()).isFalse();

                // Restore for test suite consistency
                billingProperties.getScheduler().setEnabled(true);
                assertThat(billingProperties.getScheduler().isEnabled()).isTrue();
        }

        // -------------------------------------------------------------
        // Scenario 18: Billing period uniqueness works (DB constraint protection)
        // -------------------------------------------------------------
        @Test
        @DisplayName("Scenario 18: Database uniqueness constraint prevents duplicate (subscription_id, billing_period_start)")
        void scenario18_dbUniquenessConstraintEnforced() {
                LocalDate today = LocalDate.now();

                Subscription subscription = subscriptionRepository.save(Subscription.builder()
                                .subscriptionNumber("SUB-UK-001")
                                .customer(customer)
                                .plan(monthlyPlan)
                                .quantity(1)
                                .unitPrice(new BigDecimal("200.00"))
                                .status(SubscriptionStatus.ACTIVE)
                                .startDate(today.minusMonths(1))
                                .nextBillingDate(today)
                                .build());

                Invoice inv1 = Invoice.builder()
                                .invoiceNumber("INV-UNIQ-1")
                                .customer(customer)
                                .subscription(subscription)
                                .invoiceDate(today)
                                .billingPeriodStart(today)
                                .billingPeriodEnd(today.plusMonths(1))
                                .dueDate(today.plusDays(7))
                                .status(InvoiceStatus.ISSUED)
                                .items(Collections.emptyList())
                                .build();
                invoiceRepository.saveAndFlush(inv1);

                // Attempting to insert a second invoice directly with identical
                // (subscription_id, billing_period_start)
                Invoice inv2 = Invoice.builder()
                                .invoiceNumber("INV-UNIQ-2")
                                .customer(customer)
                                .subscription(subscription)
                                .invoiceDate(today)
                                .billingPeriodStart(today)
                                .billingPeriodEnd(today.plusMonths(1))
                                .dueDate(today.plusDays(7))
                                .status(InvoiceStatus.ISSUED)
                                .items(Collections.emptyList())
                                .build();

                assertThatThrownBy(() -> invoiceRepository.saveAndFlush(inv2))
                                .isInstanceOf(DataIntegrityViolationException.class);
        }
}
package com.subscriptiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.enums.CustomerStatus;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.discount.dto.CreateDiscountRequest;
import com.subscriptiq.discount.entity.Discount;
import com.subscriptiq.discount.enums.DiscountStatus;
import com.subscriptiq.discount.enums.DiscountType;
import com.subscriptiq.discount.repository.DiscountRepository;
import com.subscriptiq.invoice.dto.CreateInvoiceItemRequest;
import com.subscriptiq.invoice.dto.CreateInvoiceRequest;
import com.subscriptiq.invoice.dto.UpdateInvoiceStatusRequest;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.entity.InvoiceItem;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.repository.InvoiceRepository;
import com.subscriptiq.payment.dto.CreatePaymentRequest;
import com.subscriptiq.payment.entity.Payment;
import com.subscriptiq.payment.enums.PaymentMethod;
import com.subscriptiq.payment.enums.PaymentStatus;
import com.subscriptiq.payment.repository.PaymentRepository;
import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.enums.BillingCycle;
import com.subscriptiq.plan.enums.PlanStatus;
import com.subscriptiq.plan.repository.PlanRepository;
import com.subscriptiq.product.entity.Product;
import com.subscriptiq.product.enums.ProductStatus;
import com.subscriptiq.product.repository.ProductRepository;
import com.subscriptiq.quotation.repository.QuotationRepository;
import com.subscriptiq.security.JwtService;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import com.subscriptiq.tax.dto.CreateTaxRequest;
import com.subscriptiq.tax.entity.Tax;
import com.subscriptiq.tax.enums.TaxStatus;
import com.subscriptiq.tax.repository.TaxRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class Phase5IntegrationTest {

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
    private com.subscriptiq.product.repository.ProductVariantRepository productVariantRepository;

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
    private TaxRepository taxRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDataCleaner dataCleaner;

    private String adminToken;
    private String staffToken;
    private String customer1Token;
    private String customer2Token;

    private Customer customer1;
    private Customer customer2;
    private Plan testPlan;

    @BeforeEach
    void setUp() {
        dataCleaner.cleanAll();

        // Admin User
        User adminUser = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
        adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), "ADMIN");

        // Staff User
        User staffUser = userRepository.save(User.builder()
                .name("Staff User")
                .email("staff@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(UserRole.STAFF)
                .status(UserStatus.ACTIVE)
                .build());
        staffToken = jwtService.generateToken(staffUser.getId(), staffUser.getEmail(), "STAFF");

        // Customer User 1
        User customerUser1 = userRepository.save(User.builder()
                .name("Customer One")
                .email("customer1@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());
        customer1Token = jwtService.generateToken(customerUser1.getId(), customerUser1.getEmail(), "CUSTOMER");

        customer1 = customerRepository.save(Customer.builder()
                .companyName("Acme Corp")
                .contactName("Alice")
                .email("alice@acme.com")
                .phone("+1234567890")
                .status(CustomerStatus.ACTIVE)
                .user(customerUser1)
                .build());

        // Customer User 2
        User customerUser2 = userRepository.save(User.builder()
                .name("Customer Two")
                .email("customer2@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());
        customer2Token = jwtService.generateToken(customerUser2.getId(), customerUser2.getEmail(), "CUSTOMER");

        customer2 = customerRepository.save(Customer.builder()
                .companyName("Beta LLC")
                .contactName("Bob")
                .email("bob@beta.com")
                .phone("+0987654321")
                .status(CustomerStatus.ACTIVE)
                .user(customerUser2)
                .build());

        // Product, Variant & Plan
        Product product = productRepository.save(Product.builder()
                .name("Cloud CRM")
                .description("Cloud CRM Software")
                .category("SAAS")
                .status(ProductStatus.ACTIVE)
                .build());

        com.subscriptiq.product.entity.ProductVariant variant = productVariantRepository.save(
                com.subscriptiq.product.entity.ProductVariant.builder()
                        .product(product)
                        .name("Standard CRM Variant")
                        .sku("CRM-STD-001")
                        .status(com.subscriptiq.product.enums.VariantStatus.ACTIVE)
                        .build()
        );

        testPlan = planRepository.save(Plan.builder()
                .productVariant(variant)
                .name("Enterprise Plan")
                .billingCycle(BillingCycle.MONTHLY)
                .price(new BigDecimal("500.00"))
                .status(PlanStatus.ACTIVE)
                .build());
    }

    // -------------------------------------------------------------
    // Scenario 1: Create active tax -> ADMIN creates, duplicate rejected
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 1: ADMIN can create tax; duplicate tax name is rejected with 400")
    void scenario1_createTaxAndRejectDuplicate() throws Exception {
        CreateTaxRequest request = CreateTaxRequest.builder()
                .name("GST 18%")
                .percentage(new BigDecimal("18.00"))
                .build();

        mockMvc.perform(post("/api/taxes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("GST 18%")))
                .andExpect(jsonPath("$.data.percentage", is(18.00)))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        // Duplicate tax name
        mockMvc.perform(post("/api/taxes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    // -------------------------------------------------------------
    // Scenario 2: Create percentage discount -> ADMIN creates, > 100 rejected
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 2: ADMIN can create percentage discount; value > 100 is rejected")
    void scenario2_createPercentageDiscount() throws Exception {
        CreateDiscountRequest validReq = CreateDiscountRequest.builder()
                .name("Early Bird 15%")
                .type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("15.00"))
                .build();

        mockMvc.perform(post("/api/discounts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("Early Bird 15%")))
                .andExpect(jsonPath("$.data.type", is("PERCENTAGE")))
                .andExpect(jsonPath("$.data.value", is(15.00)));

        CreateDiscountRequest invalidReq = CreateDiscountRequest.builder()
                .name("Excessive Discount")
                .type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("105.00"))
                .build();

        mockMvc.perform(post("/api/discounts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    // -------------------------------------------------------------
    // Scenario 3: Create fixed discount -> ADMIN creates
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 3: ADMIN can create fixed monetary discount")
    void scenario3_createFixedDiscount() throws Exception {
        CreateDiscountRequest req = CreateDiscountRequest.builder()
                .name("Voucher $50")
                .type(DiscountType.FIXED)
                .value(new BigDecimal("50.00"))
                .build();

        mockMvc.perform(post("/api/discounts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("Voucher $50")))
                .andExpect(jsonPath("$.data.type", is("FIXED")))
                .andExpect(jsonPath("$.data.value", is(50.00)));
    }

    // -------------------------------------------------------------
    // Scenario 4: Direct invoice creation -> calculated totals
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 4: Direct invoice creation calculates subtotal, tax, discount and totalAmount")
    void scenario4_directInvoiceCreation() throws Exception {
        CreateInvoiceItemRequest item1 = CreateInvoiceItemRequest.builder()
                .planId(testPlan.getId())
                .description("Monthly Enterprise Subscription")
                .quantity(2)
                .unitPrice(new BigDecimal("500.00"))
                .discountAmount(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("162.00"))
                .build();

        CreateInvoiceRequest req = CreateInvoiceRequest.builder()
                .customerId(customer1.getId())
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .notes("Direct enterprise invoice")
                .items(List.of(item1))
                .build();

        // subtotal = 2 * 500 = 1000.00
        // discount = 100.00
        // taxable = 900.00
        // tax = 162.00
        // total = 1062.00
        // balance = 1062.00
        mockMvc.perform(post("/api/invoices")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.subtotal", is(1000.00)))
                .andExpect(jsonPath("$.data.discountAmount", is(100.00)))
                .andExpect(jsonPath("$.data.taxAmount", is(162.00)))
                .andExpect(jsonPath("$.data.totalAmount", is(1062.00)))
                .andExpect(jsonPath("$.data.balanceAmount", is(1062.00)))
                .andExpect(jsonPath("$.data.status", is("DRAFT")));
    }

    // -------------------------------------------------------------
    // Scenario 5: Invoice created from subscription
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 5: Invoice created from subscription takes snapshot of plan pricing")
    void scenario5_createInvoiceFromSubscription() throws Exception {
        Subscription subscription = subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-2026-0001")
                .customer(customer1)
                .plan(testPlan)
                .quantity(1)
                .unitPrice(testPlan.getPrice())
                .discountAmount(new BigDecimal("50.00"))
                .taxAmount(new BigDecimal("81.00"))
                .totalAmount(new BigDecimal("531.00"))
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .build());

        mockMvc.perform(post("/api/subscriptions/" + subscription.getId() + "/invoice")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.subscriptionId", is(subscription.getId().intValue())))
                .andExpect(jsonPath("$.data.customerId", is(customer1.getId().intValue())))
                .andExpect(jsonPath("$.data.subtotal", is(500.00)))
                .andExpect(jsonPath("$.data.discountAmount", is(50.00)))
                .andExpect(jsonPath("$.data.taxAmount", is(81.00)))
                .andExpect(jsonPath("$.data.totalAmount", is(531.00)))
                .andExpect(jsonPath("$.data.status", is("ISSUED")));
    }

    // -------------------------------------------------------------
    // Scenario 6: Update invoice status -> DRAFT to ISSUED
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 6: Update invoice status from DRAFT to ISSUED")
    void scenario6_updateInvoiceStatus() throws Exception {
        Invoice invoice = createTestInvoice(customer1, InvoiceStatus.DRAFT, new BigDecimal("200.00"));

        UpdateInvoiceStatusRequest statusReq = UpdateInvoiceStatusRequest.builder()
                .status(InvoiceStatus.ISSUED)
                .build();

        mockMvc.perform(patch("/api/invoices/" + invoice.getId() + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("ISSUED")));
    }

    // -------------------------------------------------------------
    // Scenario 7: Cancel invoice -> ISSUED with no payments can be cancelled; with payments cannot
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 7: Cancel invoice succeeds if no payments, fails if payments exist")
    void scenario7_cancelInvoiceRules() throws Exception {
        Invoice invoice1 = createTestInvoice(customer1, InvoiceStatus.ISSUED, new BigDecimal("300.00"));

        // Cancel invoice without payments -> Success
        mockMvc.perform(post("/api/invoices/" + invoice1.getId() + "/cancel")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("CANCELLED")));

        // Invoice with payments -> Cannot be cancelled
        Invoice invoice2 = createTestInvoice(customer1, InvoiceStatus.PARTIALLY_PAID, new BigDecimal("500.00"));
        invoice2.setPaidAmount(new BigDecimal("100.00"));
        invoice2.setBalanceAmount(new BigDecimal("400.00"));
        invoiceRepository.save(invoice2);

        mockMvc.perform(post("/api/invoices/" + invoice2.getId() + "/cancel")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    // -------------------------------------------------------------
    // Scenario 8: Successful payment on invoice
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 8: Successful payment updates paidAmount and balanceAmount")
    void scenario8_successfulPayment() throws Exception {
        Invoice invoice = createTestInvoice(customer1, InvoiceStatus.ISSUED, new BigDecimal("500.00"));

        CreatePaymentRequest payReq = CreatePaymentRequest.builder()
                .amount(new BigDecimal("200.00"))
                .paymentMethod(PaymentMethod.CARD)
                .transactionReference("TXN-12345")
                .notes("Partial payment via Credit Card")
                .build();

        mockMvc.perform(post("/api/invoices/" + invoice.getId() + "/payments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.amount", is(200.00)))
                .andExpect(jsonPath("$.data.paymentMethod", is("CARD")))
                .andExpect(jsonPath("$.data.status", is("SUCCESS")));

        // Verify updated invoice
        mockMvc.perform(get("/api/invoices/" + invoice.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paidAmount", is(200.00)))
                .andExpect(jsonPath("$.data.balanceAmount", is(300.00)))
                .andExpect(jsonPath("$.data.status", is("PARTIALLY_PAID")));
    }

    // -------------------------------------------------------------
    // Scenario 9: Multiple partial payments transition to PAID
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 9: Multiple partial payments transition invoice status to PAID")
    void scenario9_multiplePaymentsTransitionToPaid() throws Exception {
        Invoice invoice = createTestInvoice(customer1, InvoiceStatus.ISSUED, new BigDecimal("300.00"));

        // 1st payment: 100 -> PARTIALLY_PAID (balance 200)
        CreatePaymentRequest pay1 = CreatePaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .build();
        mockMvc.perform(post("/api/invoices/" + invoice.getId() + "/payments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pay1)))
                .andExpect(status().isCreated());

        // 2nd payment: 200 -> PAID (balance 0)
        CreatePaymentRequest pay2 = CreatePaymentRequest.builder()
                .amount(new BigDecimal("200.00"))
                .paymentMethod(PaymentMethod.UPI)
                .build();
        mockMvc.perform(post("/api/invoices/" + invoice.getId() + "/payments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pay2)))
                .andExpect(status().isCreated());

        // Verify invoice is PAID
        mockMvc.perform(get("/api/invoices/" + invoice.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paidAmount", is(300.00)))
                .andExpect(jsonPath("$.data.balanceAmount", is(0.00)))
                .andExpect(jsonPath("$.data.status", is("PAID")));
    }

    // -------------------------------------------------------------
    // Scenario 10: Overpayment rejected
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 10: Payment exceeding balanceAmount is rejected with 400 Bad Request")
    void scenario10_overpaymentRejected() throws Exception {
        Invoice invoice = createTestInvoice(customer1, InvoiceStatus.ISSUED, new BigDecimal("200.00"));

        CreatePaymentRequest payReq = CreatePaymentRequest.builder()
                .amount(new BigDecimal("250.00"))
                .paymentMethod(PaymentMethod.CARD)
                .build();

        mockMvc.perform(post("/api/invoices/" + invoice.getId() + "/payments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    // -------------------------------------------------------------
    // Scenario 11: Payment on non-existent or cancelled invoice fails
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 11: Payment on non-existent or CANCELLED invoice fails")
    void scenario11_paymentOnCancelledOrNonExistentFails() throws Exception {
        CreatePaymentRequest payReq = CreatePaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.CASH)
                .build();

        // Non-existent invoice -> 404
        mockMvc.perform(post("/api/invoices/999999/payments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payReq)))
                .andExpect(status().isNotFound());

        // Cancelled invoice -> 400
        Invoice cancelledInvoice = createTestInvoice(customer1, InvoiceStatus.CANCELLED, new BigDecimal("200.00"));
        mockMvc.perform(post("/api/invoices/" + cancelledInvoice.getId() + "/payments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payReq)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------
    // Scenario 12: Paid invoice cannot receive further payment
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 12: Fully PAID invoice cannot receive further payments")
    void scenario12_paidInvoiceCannotReceivePayment() throws Exception {
        Invoice invoice = createTestInvoice(customer1, InvoiceStatus.PAID, new BigDecimal("200.00"));
        invoice.setPaidAmount(new BigDecimal("200.00"));
        invoice.setBalanceAmount(BigDecimal.ZERO);
        invoiceRepository.save(invoice);

        CreatePaymentRequest payReq = CreatePaymentRequest.builder()
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.CASH)
                .build();

        mockMvc.perform(post("/api/invoices/" + invoice.getId() + "/payments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payReq)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------
    // Scenario 13: Paid invoice cannot be cancelled
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 13: Fully PAID invoice cannot be cancelled")
    void scenario13_paidInvoiceCannotBeCancelled() throws Exception {
        Invoice invoice = createTestInvoice(customer1, InvoiceStatus.PAID, new BigDecimal("200.00"));
        invoice.setPaidAmount(new BigDecimal("200.00"));
        invoice.setBalanceAmount(BigDecimal.ZERO);
        invoiceRepository.save(invoice);

        mockMvc.perform(post("/api/invoices/" + invoice.getId() + "/cancel")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------
    // Scenario 14: Overdue calculation
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 14: Invoice with past due date and outstanding balance is recognized as OVERDUE")
    void scenario14_overdueCalculation() throws Exception {
        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-2026-OVERDUE")
                .customer(customer1)
                .invoiceDate(LocalDate.now().minusDays(30))
                .dueDate(LocalDate.now().minusDays(5))
                .status(InvoiceStatus.ISSUED)
                .subtotal(new BigDecimal("400.00"))
                .totalAmount(new BigDecimal("400.00"))
                .paidAmount(BigDecimal.ZERO)
                .balanceAmount(new BigDecimal("400.00"))
                .build();
        invoiceRepository.save(invoice);

        mockMvc.perform(get("/api/invoices/" + invoice.getId())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("OVERDUE")));
    }

    // -------------------------------------------------------------
    // Scenario 15: Staff can view active taxes and discounts
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 15: STAFF role can view active taxes and discounts")
    void scenario15_staffCanViewActiveTaxesAndDiscounts() throws Exception {
        taxRepository.save(Tax.builder()
                .name("VAT 10%")
                .percentage(new BigDecimal("10.00"))
                .status(TaxStatus.ACTIVE)
                .build());

        discountRepository.save(Discount.builder()
                .name("Winter Promo")
                .type(DiscountType.FIXED)
                .value(new BigDecimal("25.00"))
                .status(DiscountStatus.ACTIVE)
                .build());

        mockMvc.perform(get("/api/taxes/active")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        mockMvc.perform(get("/api/discounts/active")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    // -------------------------------------------------------------
    // Scenario 16: Customer cannot manage taxes or discounts
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 16: CUSTOMER role is forbidden from managing taxes and discounts")
    void scenario16_customerForbiddenFromManagingTaxesAndDiscounts() throws Exception {
        CreateTaxRequest taxReq = CreateTaxRequest.builder()
                .name("Illegal Tax")
                .percentage(new BigDecimal("5.00"))
                .build();

        mockMvc.perform(post("/api/taxes")
                        .header("Authorization", "Bearer " + customer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taxReq)))
                .andExpect(status().isForbidden());

        CreateDiscountRequest discReq = CreateDiscountRequest.builder()
                .name("Illegal Discount")
                .type(DiscountType.FIXED)
                .value(new BigDecimal("10.00"))
                .build();

        mockMvc.perform(post("/api/discounts")
                        .header("Authorization", "Bearer " + customer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discReq)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------
    // Scenario 17: Customer can view their own invoices
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 17: CUSTOMER can view their own invoices")
    void scenario17_customerCanViewOwnInvoices() throws Exception {
        Invoice invoice1 = createTestInvoice(customer1, InvoiceStatus.ISSUED, new BigDecimal("250.00"));

        mockMvc.perform(get("/api/invoices/" + invoice1.getId())
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(invoice1.getId().intValue())));

        mockMvc.perform(get("/api/customers/" + customer1.getId() + "/invoices")
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)));
    }

    // -------------------------------------------------------------
    // Scenario 18: Customer cannot view other customer's invoices
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 18: CUSTOMER is forbidden from viewing other customer's invoices")
    void scenario18_customerCannotViewOtherCustomerInvoices() throws Exception {
        Invoice customer2Invoice = createTestInvoice(customer2, InvoiceStatus.ISSUED, new BigDecimal("350.00"));

        // Direct lookup by ID -> 403
        mockMvc.perform(get("/api/invoices/" + customer2Invoice.getId())
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isForbidden());

        // Lookup by customer ID endpoint -> 403
        mockMvc.perform(get("/api/customers/" + customer2.getId() + "/invoices")
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------
    // Scenario 19: Customer can view their own payments
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 19: CUSTOMER can view their own payments")
    void scenario19_customerCanViewOwnPayments() throws Exception {
        Invoice invoice = createTestInvoice(customer1, InvoiceStatus.ISSUED, new BigDecimal("500.00"));

        Payment payment = paymentRepository.save(Payment.builder()
                .paymentNumber("PAY-2026-0001")
                .invoice(invoice)
                .customer(customer1)
                .amount(new BigDecimal("200.00"))
                .paymentDate(LocalDateTime.now())
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.SUCCESS)
                .build());

        mockMvc.perform(get("/api/payments/" + payment.getId())
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount", is(200.00)));

        mockMvc.perform(get("/api/customers/" + customer1.getId() + "/payments")
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)));
    }

    // -------------------------------------------------------------
    // Scenario 20: Customer cannot view other customer's payments
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 20: CUSTOMER is forbidden from viewing other customer's payments")
    void scenario20_customerCannotViewOtherCustomerPayments() throws Exception {
        Invoice invoice2 = createTestInvoice(customer2, InvoiceStatus.ISSUED, new BigDecimal("500.00"));

        Payment payment2 = paymentRepository.save(Payment.builder()
                .paymentNumber("PAY-2026-0002")
                .invoice(invoice2)
                .customer(customer2)
                .amount(new BigDecimal("300.00"))
                .paymentDate(LocalDateTime.now())
                .paymentMethod(PaymentMethod.UPI)
                .status(PaymentStatus.SUCCESS)
                .build());

        // By ID -> 403
        mockMvc.perform(get("/api/payments/" + payment2.getId())
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isForbidden());

        // By Customer ID -> 403
        mockMvc.perform(get("/api/customers/" + customer2.getId() + "/payments")
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------
    // Scenario 21: Financial integrity check
    // -------------------------------------------------------------
    @Test
    @DisplayName("Scenario 21: Financial integrity check: subtotal - discount + tax = total, paid + balance = total")
    void scenario21_financialIntegrityCheck() throws Exception {
        // Create an invoice with 2 items
        CreateInvoiceItemRequest item1 = CreateInvoiceItemRequest.builder()
                .planId(testPlan.getId())
                .quantity(3)
                .unitPrice(new BigDecimal("120.00"))     // subtotal = 360.00
                .discountAmount(new BigDecimal("40.00"))  // taxable = 320.00
                .taxAmount(new BigDecimal("32.00"))       // item1 total = 352.00
                .build();

        CreateInvoiceItemRequest item2 = CreateInvoiceItemRequest.builder()
                .quantity(1)
                .unitPrice(new BigDecimal("250.00"))     // subtotal = 250.00
                .discountAmount(new BigDecimal("50.00"))  // taxable = 200.00
                .taxAmount(new BigDecimal("20.00"))       // item2 total = 220.00
                .build();

        CreateInvoiceRequest req = CreateInvoiceRequest.builder()
                .customerId(customer1.getId())
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .items(List.of(item1, item2))
                .build();

        String createResp = mockMvc.perform(post("/api/invoices")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long invoiceId = objectMapper.readTree(createResp).get("data").get("id").asLong();

        // Issue invoice
        mockMvc.perform(patch("/api/invoices/" + invoiceId + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateInvoiceStatusRequest.builder()
                                .status(InvoiceStatus.ISSUED).build())))
                .andExpect(status().isOk());

        // Make partial payment of 200.00
        CreatePaymentRequest pay1 = CreatePaymentRequest.builder()
                .amount(new BigDecimal("200.00"))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .build();
        mockMvc.perform(post("/api/invoices/" + invoiceId + "/payments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pay1)))
                .andExpect(status().isCreated());

        // Make second payment of 372.00 (exact remaining balance)
        CreatePaymentRequest pay2 = CreatePaymentRequest.builder()
                .amount(new BigDecimal("372.00"))
                .paymentMethod(PaymentMethod.CARD)
                .build();
        mockMvc.perform(post("/api/invoices/" + invoiceId + "/payments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pay2)))
                .andExpect(status().isCreated());

        // Fetch finalized invoice
        mockMvc.perform(get("/api/invoices/" + invoiceId)
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subtotal", is(610.00)))
                .andExpect(jsonPath("$.data.discountAmount", is(90.00)))
                .andExpect(jsonPath("$.data.taxAmount", is(52.00)))
                .andExpect(jsonPath("$.data.totalAmount", is(572.00)))
                .andExpect(jsonPath("$.data.paidAmount", is(572.00)))
                .andExpect(jsonPath("$.data.balanceAmount", is(0.00)))
                .andExpect(jsonPath("$.data.status", is("PAID")));
    }

    private Invoice createTestInvoice(Customer customer, InvoiceStatus status, BigDecimal amount) {
        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-TEST-" + System.nanoTime())
                .customer(customer)
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .status(status)
                .subtotal(amount)
                .totalAmount(amount)
                .paidAmount(BigDecimal.ZERO)
                .balanceAmount(amount)
                .items(Collections.emptyList())
                .build();
        return invoiceRepository.save(invoice);
    }
}

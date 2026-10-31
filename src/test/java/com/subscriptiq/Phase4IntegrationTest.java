package com.subscriptiq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.enums.CustomerStatus;
import com.subscriptiq.customer.repository.CustomerRepository;
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
import com.subscriptiq.quotation.dto.CreateQuotationItemRequest;
import com.subscriptiq.quotation.dto.CreateQuotationRequest;
import com.subscriptiq.quotation.dto.UpdateQuotationRequest;
import com.subscriptiq.quotation.dto.UpdateQuotationStatusRequest;
import com.subscriptiq.quotation.entity.Quotation;
import com.subscriptiq.quotation.entity.QuotationItem;
import com.subscriptiq.quotation.enums.QuotationStatus;
import com.subscriptiq.quotation.repository.QuotationRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
public class Phase4IntegrationTest {

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
    private Plan activePlan;

    @BeforeEach
    void setUp() {
        dataCleaner.cleanAll();

        // 1. Create Admin
        User adminUser = userRepository.save(User.builder()
                .name("Admin Tester")
                .email("admin@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
        adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), "ADMIN");

        // 2. Create Staff
        User staffUser = userRepository.save(User.builder()
                .name("Staff Tester")
                .email("staff@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(UserRole.STAFF)
                .status(UserStatus.ACTIVE)
                .build());
        staffToken = jwtService.generateToken(staffUser.getId(), staffUser.getEmail(), "STAFF");

        // 3. Create Customer 1 User & Entity
        User custUser1 = userRepository.save(User.builder()
                .name("Customer One")
                .email("cust1@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());
        customer1Token = jwtService.generateToken(custUser1.getId(), custUser1.getEmail(), "CUSTOMER");

        customer1 = customerRepository.save(Customer.builder()
                .user(custUser1)
                .companyName("Acme One Corp")
                .contactName("Alice Smith")
                .email("alice@acme.com")
                .status(CustomerStatus.ACTIVE)
                .build());

        // 4. Create Customer 2 User & Entity
        User custUser2 = userRepository.save(User.builder()
                .name("Customer Two")
                .email("cust2@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());
        customer2Token = jwtService.generateToken(custUser2.getId(), custUser2.getEmail(), "CUSTOMER");

        customer2 = customerRepository.save(Customer.builder()
                .user(custUser2)
                .companyName("Beta Two Corp")
                .contactName("Bob Jones")
                .email("bob@beta.com")
                .status(CustomerStatus.ACTIVE)
                .build());

        // 5. Create Catalog Hierarchy
        Product product = productRepository.save(Product.builder()
                .name("Enterprise Cloud Platform")
                .category("SAAS")
                .status(ProductStatus.ACTIVE)
                .build());

        ProductVariant variant = productVariantRepository.save(ProductVariant.builder()
                .product(product)
                .name("Multi-Tenant Standard")
                .sku("ECP-STD-001")
                .status(VariantStatus.ACTIVE)
                .build());

        activePlan = planRepository.save(Plan.builder()
                .productVariant(variant)
                .name("Monthly Pro")
                .billingCycle(BillingCycle.MONTHLY)
                .price(new BigDecimal("500.00"))
                .currency("INR")
                .status(PlanStatus.ACTIVE)
                .build());
    }

    // ==========================================
    // 1. Create Quotation
    // ==========================================
    @Test
    @DisplayName("1. Create quotation successfully")
    void test1_CreateQuotation_Success() throws Exception {
        CreateQuotationRequest request = CreateQuotationRequest.builder()
                .customerId(customer1.getId())
                .quotationDate(LocalDate.now())
                .validUntil(LocalDate.now().plusDays(30))
                .notes("Initial 30-day quote")
                .items(List.of(
                        CreateQuotationItemRequest.builder()
                                .planId(activePlan.getId())
                                .quantity(2)
                                .discountAmount(new BigDecimal("50.00"))
                                .taxAmount(new BigDecimal("18.00"))
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/quotations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.subtotal").value(1000.00)) // 500 * 2
                .andExpect(jsonPath("$.data.discountAmount").value(50.00))
                .andExpect(jsonPath("$.data.taxAmount").value(18.00))
                .andExpect(jsonPath("$.data.totalAmount").value(968.00)); // 1000 - 50 + 18
    }

    // ==========================================
    // 2. Get Quotation
    // ==========================================
    @Test
    @DisplayName("2. Get quotation by ID")
    void test2_GetQuotationById() throws Exception {
        Quotation quotation = createSampleQuotation(customer1, QuotationStatus.DRAFT, LocalDate.now().plusDays(15));

        mockMvc.perform(get("/api/quotations/" + quotation.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(quotation.getId()))
                .andExpect(jsonPath("$.data.quotationNumber").value(quotation.getQuotationNumber()));
    }

    // ==========================================
    // 3. Update Quotation
    // ==========================================
    @Test
    @DisplayName("3. Update DRAFT quotation successfully")
    void test3_UpdateQuotation_Success() throws Exception {
        Quotation quotation = createSampleQuotation(customer1, QuotationStatus.DRAFT, LocalDate.now().plusDays(15));

        UpdateQuotationRequest updateRequest = UpdateQuotationRequest.builder()
                .notes("Updated notes with revised discounts")
                .items(List.of(
                        CreateQuotationItemRequest.builder()
                                .planId(activePlan.getId())
                                .quantity(3)
                                .discountAmount(new BigDecimal("100.00"))
                                .taxAmount(new BigDecimal("20.00"))
                                .build()
                ))
                .build();

        mockMvc.perform(put("/api/quotations/" + quotation.getId())
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notes").value("Updated notes with revised discounts"))
                .andExpect(jsonPath("$.data.subtotal").value(1500.00)) // 500 * 3
                .andExpect(jsonPath("$.data.totalAmount").value(1420.00)); // 1500 - 100 + 20
    }

    // ==========================================
    // 4. Send Quotation (DRAFT -> SENT)
    // ==========================================
    @Test
    @DisplayName("4. Send quotation transitions status to SENT")
    void test4_SendQuotation_Success() throws Exception {
        Quotation quotation = createSampleQuotation(customer1, QuotationStatus.DRAFT, LocalDate.now().plusDays(15));

        UpdateQuotationStatusRequest statusRequest = new UpdateQuotationStatusRequest(QuotationStatus.SENT);

        mockMvc.perform(patch("/api/quotations/" + quotation.getId() + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SENT"));
    }

    // ==========================================
    // 5. Accept Quotation (SENT -> ACCEPTED)
    // ==========================================
    @Test
    @DisplayName("5. Accept quotation transitions status to ACCEPTED")
    void test5_AcceptQuotation_Success() throws Exception {
        Quotation quotation = createSampleQuotation(customer1, QuotationStatus.SENT, LocalDate.now().plusDays(15));

        UpdateQuotationStatusRequest statusRequest = new UpdateQuotationStatusRequest(QuotationStatus.ACCEPTED);

        mockMvc.perform(patch("/api/quotations/" + quotation.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    // ==========================================
    // 6. Reject Quotation (SENT -> REJECTED & DRAFT -> REJECTED)
    // ==========================================
    @Test
    @DisplayName("6. Reject quotation transitions status to REJECTED")
    void test6_RejectQuotation_Success() throws Exception {
        Quotation quotation = createSampleQuotation(customer1, QuotationStatus.SENT, LocalDate.now().plusDays(15));

        UpdateQuotationStatusRequest statusRequest = new UpdateQuotationStatusRequest(QuotationStatus.REJECTED);

        mockMvc.perform(patch("/api/quotations/" + quotation.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    // ==========================================
    // 7. Invalid Status Transition (DRAFT -> ACCEPTED)
    // ==========================================
    @Test
    @DisplayName("7. Invalid transition DRAFT to ACCEPTED returns 400 Bad Request")
    void test7_InvalidStatusTransition_Returns400() throws Exception {
        Quotation quotation = createSampleQuotation(customer1, QuotationStatus.DRAFT, LocalDate.now().plusDays(15));

        UpdateQuotationStatusRequest statusRequest = new UpdateQuotationStatusRequest(QuotationStatus.ACCEPTED);

        mockMvc.perform(patch("/api/quotations/" + quotation.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid status transition from DRAFT to ACCEPTED"));
    }

    // ==========================================
    // 8. Expired Quotation Cannot Be Accepted
    // ==========================================
    @Test
    @DisplayName("8. Expired quotation cannot be accepted")
    void test8_ExpiredQuotation_CannotBeAccepted() throws Exception {
        Quotation quotation = createSampleQuotation(customer1, QuotationStatus.SENT, LocalDate.now().minusDays(5));

        UpdateQuotationStatusRequest statusRequest = new UpdateQuotationStatusRequest(QuotationStatus.ACCEPTED);

        mockMvc.perform(patch("/api/quotations/" + quotation.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quotation has expired on " + quotation.getValidUntil() + " and cannot be accepted"));
    }

    // ==========================================
    // 9. Convert Accepted Quotation to Subscription
    // ==========================================
    @Test
    @DisplayName("9. Convert accepted quotation to subscription successfully")
    void test9_ConvertQuotation_Success() throws Exception {
        Quotation quotation = createSampleQuotation(customer1, QuotationStatus.ACCEPTED, LocalDate.now().plusDays(20));

        mockMvc.perform(post("/api/quotations/" + quotation.getId() + "/convert")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.quotationId").value(quotation.getId()))
                .andExpect(jsonPath("$.data.customerId").value(customer1.getId()))
                .andExpect(jsonPath("$.data.unitPrice").value(500.00))
                .andExpect(jsonPath("$.data.totalAmount").value(500.00));

        Quotation updatedQuotation = quotationRepository.findById(quotation.getId()).orElseThrow();
        assertEquals(QuotationStatus.CONVERTED, updatedQuotation.getStatus());
    }

    // ==========================================
    // 10. Cannot Convert Rejected Quotation
    // ==========================================
    @Test
    @DisplayName("10. Cannot convert REJECTED quotation")
    void test10_CannotConvertRejectedQuotation() throws Exception {
        Quotation quotation = createSampleQuotation(customer1, QuotationStatus.REJECTED, LocalDate.now().plusDays(20));

        mockMvc.perform(post("/api/quotations/" + quotation.getId() + "/convert")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot convert a REJECTED quotation"));
    }

    // ==========================================
    // 11. Cannot Convert Expired Quotation
    // ==========================================
    @Test
    @DisplayName("11. Cannot convert EXPIRED quotation")
    void test11_CannotConvertExpiredQuotation() throws Exception {
        Quotation quotation = createSampleQuotation(customer1, QuotationStatus.ACCEPTED, LocalDate.now().minusDays(2));

        mockMvc.perform(post("/api/quotations/" + quotation.getId() + "/convert")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot convert an EXPIRED quotation"));
    }

    // ==========================================
    // 12. Cannot Convert Same Quotation Twice
    // ==========================================
    @Test
    @DisplayName("12. Cannot convert already CONVERTED quotation twice")
    void test12_CannotConvertSameQuotationTwice() throws Exception {
        Quotation quotation = createSampleQuotation(customer1, QuotationStatus.ACCEPTED, LocalDate.now().plusDays(20));

        // First conversion
        mockMvc.perform(post("/api/quotations/" + quotation.getId() + "/convert")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated());

        // Second conversion attempt
        mockMvc.perform(post("/api/quotations/" + quotation.getId() + "/convert")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quotation has already been converted to a subscription"));
    }

    // ==========================================
    // 13. Subscription Pricing Snapshot
    // ==========================================
    @Test
    @DisplayName("13. Subscription preserves pricing snapshot after catalog plan price updates")
    void test13_SubscriptionPricingSnapshotPreserved() throws Exception {
        Quotation quotation = createSampleQuotation(customer1, QuotationStatus.ACCEPTED, LocalDate.now().plusDays(20));

        String res = mockMvc.perform(post("/api/quotations/" + quotation.getId() + "/convert")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(res);
        Long subId = jsonNode.get("data").get("id").asLong();

        // Update plan catalog price in database to 999.00
        activePlan.setPrice(new BigDecimal("999.00"));
        planRepository.save(activePlan);

        // Fetch subscription and confirm unitPrice is still the snapshot price 500.00
        mockMvc.perform(get("/api/subscriptions/" + subId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unitPrice").value(500.00));
    }

    // ==========================================
    // 14. Pause Subscription (ACTIVE -> PAUSED)
    // ==========================================
    @Test
    @DisplayName("14. Pause ACTIVE subscription transitions to PAUSED")
    void test14_PauseSubscription() throws Exception {
        Subscription subscription = createSampleSubscription(customer1, SubscriptionStatus.ACTIVE);

        mockMvc.perform(patch("/api/subscriptions/" + subscription.getId() + "/pause")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAUSED"));
    }

    // ==========================================
    // 15. Resume Subscription (PAUSED -> ACTIVE)
    // ==========================================
    @Test
    @DisplayName("15. Resume PAUSED subscription transitions back to ACTIVE")
    void test15_ResumeSubscription() throws Exception {
        Subscription subscription = createSampleSubscription(customer1, SubscriptionStatus.PAUSED);

        mockMvc.perform(patch("/api/subscriptions/" + subscription.getId() + "/resume")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    // ==========================================
    // 16. Close Subscription (ACTIVE -> CLOSED)
    // ==========================================
    @Test
    @DisplayName("16. Close subscription sets status to CLOSED")
    void test16_CloseSubscription() throws Exception {
        Subscription subscription = createSampleSubscription(customer1, SubscriptionStatus.ACTIVE);

        mockMvc.perform(patch("/api/subscriptions/" + subscription.getId() + "/close")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"))
                .andExpect(jsonPath("$.data.endDate").isNotEmpty());
    }

    // ==========================================
    // 17. Customer Cannot Access Another Customer's Quotation
    // ==========================================
    @Test
    @DisplayName("17. Customer 2 cannot access Customer 1's quotation (403 Forbidden)")
    void test17_CustomerCannotAccessAnotherCustomerQuotation() throws Exception {
        Quotation quotation = createSampleQuotation(customer1, QuotationStatus.DRAFT, LocalDate.now().plusDays(15));

        // Attempt direct access by ID
        mockMvc.perform(get("/api/quotations/" + quotation.getId())
                        .header("Authorization", "Bearer " + customer2Token))
                .andExpect(status().isForbidden());

        // Attempt customer directory access
        mockMvc.perform(get("/api/customers/" + customer1.getId() + "/quotations")
                        .header("Authorization", "Bearer " + customer2Token))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 18. Customer Cannot Access Another Customer's Subscription
    // ==========================================
    @Test
    @DisplayName("18. Customer 2 cannot access Customer 1's subscription (403 Forbidden)")
    void test18_CustomerCannotAccessAnotherCustomerSubscription() throws Exception {
        Subscription subscription = createSampleSubscription(customer1, SubscriptionStatus.ACTIVE);

        // Attempt direct access by ID
        mockMvc.perform(get("/api/subscriptions/" + subscription.getId())
                        .header("Authorization", "Bearer " + customer2Token))
                .andExpect(status().isForbidden());

        // Attempt customer directory access
        mockMvc.perform(get("/api/customers/" + customer1.getId() + "/subscriptions")
                        .header("Authorization", "Bearer " + customer2Token))
                .andExpect(status().isForbidden());

        // Customer 1 querying own customer subscriptions succeeds
        mockMvc.perform(get("/api/customers/" + customer1.getId() + "/subscriptions")
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(subscription.getId()));
    }

    // ==========================================
    // 19. Validation Failures
    // ==========================================
    @Test
    @DisplayName("19. Validation failures on invalid quantity, negative amounts, and invalid dates")
    void test19_ValidationFailures() throws Exception {
        // Zero quantity
        CreateQuotationRequest badQuantity = CreateQuotationRequest.builder()
                .customerId(customer1.getId())
                .validUntil(LocalDate.now().plusDays(10))
                .items(List.of(
                        CreateQuotationItemRequest.builder().planId(activePlan.getId()).quantity(0).build()
                ))
                .build();

        mockMvc.perform(post("/api/quotations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badQuantity)))
                .andExpect(status().isBadRequest());

        // Negative discount
        CreateQuotationRequest negativeDiscount = CreateQuotationRequest.builder()
                .customerId(customer1.getId())
                .validUntil(LocalDate.now().plusDays(10))
                .items(List.of(
                        CreateQuotationItemRequest.builder()
                                .planId(activePlan.getId())
                                .quantity(1)
                                .discountAmount(new BigDecimal("-20.00"))
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/quotations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(negativeDiscount)))
                .andExpect(status().isBadRequest());

        // validUntil before quotationDate
        CreateQuotationRequest badDates = CreateQuotationRequest.builder()
                .customerId(customer1.getId())
                .quotationDate(LocalDate.now())
                .validUntil(LocalDate.now().minusDays(1))
                .items(List.of(
                        CreateQuotationItemRequest.builder().planId(activePlan.getId()).quantity(1).build()
                ))
                .build();

        mockMvc.perform(post("/api/quotations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badDates)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("validUntil date cannot be before quotationDate"));
    }

    // ==========================================
    // 20. Renew Subscription
    // ==========================================
    @Test
    @DisplayName("20. Renew ACTIVE subscription advances next billing date")
    void test20_RenewSubscription_Success() throws Exception {
        Subscription subscription = createSampleSubscription(customer1, SubscriptionStatus.ACTIVE);
        LocalDate oldBillingDate = subscription.getNextBillingDate();

        mockMvc.perform(patch("/api/subscriptions/" + subscription.getId() + "/renew")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Subscription renewed = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        assertEquals(oldBillingDate.plusMonths(1), renewed.getNextBillingDate());
    }

    // Helper to generate a test quotation
    private Quotation createSampleQuotation(Customer customer, QuotationStatus status, LocalDate validUntil) {
        String num = "QT-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        Quotation quotation = Quotation.builder()
                .quotationNumber(num)
                .customer(customer)
                .status(status)
                .quotationDate(LocalDate.now())
                .validUntil(validUntil)
                .subtotal(new BigDecimal("500.00"))
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("500.00"))
                .items(new ArrayList<>())
                .build();

        QuotationItem item = QuotationItem.builder()
                .plan(activePlan)
                .quantity(1)
                .unitPrice(new BigDecimal("500.00"))
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("500.00"))
                .build();

        quotation.addItem(item);
        return quotationRepository.save(quotation);
    }

    // Helper to generate a test subscription
    private Subscription createSampleSubscription(Customer customer, SubscriptionStatus status) {
        String subNum = "SUB-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        Subscription subscription = Subscription.builder()
                .subscriptionNumber(subNum)
                .customer(customer)
                .plan(activePlan)
                .status(status)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .quantity(1)
                .unitPrice(new BigDecimal("500.00"))
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("500.00"))
                .build();

        return subscriptionRepository.save(subscription);
    }
}

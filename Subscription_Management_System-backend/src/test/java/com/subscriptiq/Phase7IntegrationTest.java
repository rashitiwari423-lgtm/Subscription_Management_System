package com.subscriptiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.enums.CustomerStatus;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.optimization.dto.AnalyzeUsageRequest;
import com.subscriptiq.optimization.entity.OptimizationInsight;
import com.subscriptiq.optimization.enums.OptimizationRecommendation;
import com.subscriptiq.optimization.enums.OptimizationStatus;
import com.subscriptiq.optimization.repository.OptimizationInsightRepository;
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
import com.subscriptiq.usage.dto.CreateUsageRecordRequest;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class Phase7IntegrationTest {

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
    private ProductVariantRepository variantRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UsageRecordRepository usageRecordRepository;

    @Autowired
    private OptimizationInsightRepository optimizationInsightRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User adminUser;
    private User staffUser;
    private User customerUser1;
    private User customerUser2;

    private String adminToken;
    private String staffToken;
    private String customer1Token;
    private String customer2Token;

    private Customer customer1;
    private Customer customer2;

    private Plan standardPlan;
    private Subscription subscription1;
    private Subscription subscription2;

    @BeforeEach
    void setUp() {
        dataCleaner.cleanAll();

        adminUser = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        staffUser = userRepository.save(User.builder()
                .name("Staff Member")
                .email("staff@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(UserRole.STAFF)
                .status(UserStatus.ACTIVE)
                .build());

        customerUser1 = userRepository.save(User.builder()
                .name("Customer One")
                .email("cust1@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());

        customerUser2 = userRepository.save(User.builder()
                .name("Customer Two")
                .email("cust2@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());

        adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), "ADMIN");
        staffToken = jwtService.generateToken(staffUser.getId(), staffUser.getEmail(), "STAFF");
        customer1Token = jwtService.generateToken(customerUser1.getId(), customerUser1.getEmail(), "CUSTOMER");
        customer2Token = jwtService.generateToken(customerUser2.getId(), customerUser2.getEmail(), "CUSTOMER");

        customer1 = customerRepository.save(Customer.builder()
                .companyName("Acme Corp")
                .contactName("Alice Smith")
                .email("alice@acme.com")
                .phone("1234567890")
                .city("New York")
                .state("NY")
                .country("USA")
                .status(CustomerStatus.ACTIVE)
                .user(customerUser1)
                .build());

        customer2 = customerRepository.save(Customer.builder()
                .companyName("Beta LLC")
                .contactName("Bob Jones")
                .email("bob@beta.com")
                .phone("0987654321")
                .city("Chicago")
                .state("IL")
                .country("USA")
                .status(CustomerStatus.ACTIVE)
                .user(customerUser2)
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Productivity Suite")
                .category("SOFTWARE")
                .status(ProductStatus.ACTIVE)
                .build());

        ProductVariant variant = variantRepository.save(ProductVariant.builder()
                .product(product)
                .name("Enterprise Variant")
                .sku("SKU-PROD-ENT")
                .status(VariantStatus.ACTIVE)
                .build());

        standardPlan = planRepository.save(Plan.builder()
                .productVariant(variant)
                .name("Enterprise 50-Seat Plan")
                .billingCycle(BillingCycle.MONTHLY)
                .price(new BigDecimal("2000.00"))
                .status(PlanStatus.ACTIVE)
                .build());

        subscription1 = subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-20261031-1001")
                .customer(customer1)
                .plan(standardPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .nextBillingDate(LocalDate.of(2026, 2, 1))
                .quantity(50)
                .unitPrice(new BigDecimal("2000.00"))
                .totalAmount(new BigDecimal("100000.00"))
                .build());

        subscription2 = subscriptionRepository.save(Subscription.builder()
                .subscriptionNumber("SUB-20261031-2002")
                .customer(customer2)
                .plan(standardPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .nextBillingDate(LocalDate.of(2026, 2, 1))
                .quantity(100)
                .unitPrice(new BigDecimal("2000.00"))
                .totalAmount(new BigDecimal("200000.00"))
                .build());
    }

    // ==========================================
    // 1. USAGE TRACKING & VALIDATION TESTS
    // ==========================================

    @Test
    @DisplayName("Scenario 1: STAFF/ADMIN successfully records usage snapshot with deterministic calculations")
    void scenario1_recordUsage_Success() throws Exception {
        CreateUsageRecordRequest request = CreateUsageRecordRequest.builder()
                .customerId(customer1.getId())
                .subscriptionId(subscription1.getId())
                .resourceType("LICENSE")
                .resourceName("Microsoft 365 Licenses")
                .allocatedQuantity(50)
                .usedQuantity(32)
                .unitCost(new BigDecimal("2000.00"))
                .snapshotDate(LocalDate.of(2026, 10, 31))
                .build();

        mockMvc.perform(post("/api/usage")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.allocatedQuantity", is(50)))
                .andExpect(jsonPath("$.data.usedQuantity", is(32)))
                .andExpect(jsonPath("$.data.unusedQuantity", is(18)))
                .andExpect(jsonPath("$.data.utilizationPercentage", is(64.00)))
                .andExpect(jsonPath("$.data.unitCost", is(2000.00)))
                .andExpect(jsonPath("$.data.resourceName", is("Microsoft 365 Licenses")));
    }

    @Test
    @DisplayName("Scenario 2: Usage validation - negative allocated quantity is rejected (400)")
    void scenario2_negativeAllocatedQuantity_Rejected() throws Exception {
        CreateUsageRecordRequest request = CreateUsageRecordRequest.builder()
                .customerId(customer1.getId())
                .subscriptionId(subscription1.getId())
                .resourceType("LICENSE")
                .resourceName("Licenses")
                .allocatedQuantity(-5)
                .usedQuantity(0)
                .snapshotDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/usage")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("Scenario 3: Usage validation - negative used quantity is rejected (400)")
    void scenario3_negativeUsedQuantity_Rejected() throws Exception {
        CreateUsageRecordRequest request = CreateUsageRecordRequest.builder()
                .customerId(customer1.getId())
                .subscriptionId(subscription1.getId())
                .resourceType("LICENSE")
                .resourceName("Licenses")
                .allocatedQuantity(50)
                .usedQuantity(-1)
                .snapshotDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/usage")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("Scenario 4: Usage validation - used quantity exceeding allocated quantity is rejected (400)")
    void scenario4_usedExceedsAllocated_Rejected() throws Exception {
        CreateUsageRecordRequest request = CreateUsageRecordRequest.builder()
                .customerId(customer1.getId())
                .subscriptionId(subscription1.getId())
                .resourceType("LICENSE")
                .resourceName("Licenses")
                .allocatedQuantity(50)
                .usedQuantity(55)
                .snapshotDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/usage")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("must not exceed allocatedQuantity")));
    }

    @Test
    @DisplayName("Scenario 5: Usage validation - non-existent customer rejected (404)")
    void scenario5_missingCustomer_Rejected() throws Exception {
        CreateUsageRecordRequest request = CreateUsageRecordRequest.builder()
                .customerId(99999L)
                .subscriptionId(subscription1.getId())
                .resourceType("LICENSE")
                .resourceName("Licenses")
                .allocatedQuantity(50)
                .usedQuantity(30)
                .snapshotDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/usage")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("Scenario 6: Usage validation - non-existent subscription rejected (404)")
    void scenario6_missingSubscription_Rejected() throws Exception {
        CreateUsageRecordRequest request = CreateUsageRecordRequest.builder()
                .customerId(customer1.getId())
                .subscriptionId(99999L)
                .resourceType("LICENSE")
                .resourceName("Licenses")
                .allocatedQuantity(50)
                .usedQuantity(30)
                .snapshotDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/usage")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("Scenario 7: Usage validation - subscription does not belong to customer (400)")
    void scenario7_subscriptionCustomerMismatch_Rejected() throws Exception {
        // subscription2 belongs to customer2, but request specifies customer1
        CreateUsageRecordRequest request = CreateUsageRecordRequest.builder()
                .customerId(customer1.getId())
                .subscriptionId(subscription2.getId())
                .resourceType("LICENSE")
                .resourceName("Licenses")
                .allocatedQuantity(50)
                .usedQuantity(30)
                .snapshotDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/usage")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("does not belong to customer")));
    }

    // ==========================================
    // 2. USAGE RBAC & OWNERSHIP TESTS
    // ==========================================

    @Test
    @DisplayName("Scenario 8: GET /api/usage/{id} - CUSTOMER 1 can view own record, CUSTOMER 2 gets 403 Forbidden")
    void scenario8_customerUsageOwnership() throws Exception {
        UsageRecord record = usageRecordRepository.save(UsageRecord.builder()
                .customer(customer1)
                .subscription(subscription1)
                .resourceType("LICENSE")
                .resourceName("Microsoft 365 Licenses")
                .allocatedQuantity(50)
                .usedQuantity(32)
                .unitCost(new BigDecimal("2000.00"))
                .snapshotDate(LocalDate.now())
                .build());

        // CUSTOMER 1 accesses own record -> 200 OK
        mockMvc.perform(get("/api/usage/" + record.getId())
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(record.getId().intValue())))
                .andExpect(jsonPath("$.data.customerId", is(customer1.getId().intValue())));

        // CUSTOMER 2 accesses CUSTOMER 1's record -> 403 Forbidden
        mockMvc.perform(get("/api/usage/" + record.getId())
                        .header("Authorization", "Bearer " + customer2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Scenario 9: GET /api/usage - ADMIN/STAFF can list all usage; CUSTOMER forbidden (403)")
    void scenario9_listAllUsageRbac() throws Exception {
        usageRecordRepository.save(UsageRecord.builder()
                .customer(customer1)
                .subscription(subscription1)
                .resourceType("LICENSE")
                .resourceName("Seats")
                .allocatedQuantity(10)
                .usedQuantity(5)
                .snapshotDate(LocalDate.now())
                .build());

        // STAFF -> 200 OK
        mockMvc.perform(get("/api/usage")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", notNullValue()));

        // CUSTOMER -> 403 Forbidden
        mockMvc.perform(get("/api/usage")
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Scenario 10: GET /api/usage/customer/{customerId} - Customer can access own, forbidden for others")
    void scenario10_customerScopedUsageAccess() throws Exception {
        mockMvc.perform(get("/api/usage/customer/" + customer1.getId())
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/usage/customer/" + customer2.getId())
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Scenario 11: GET /api/usage/subscription/{subscriptionId} - Customer can access own, forbidden for others")
    void scenario11_subscriptionScopedUsageAccess() throws Exception {
        mockMvc.perform(get("/api/usage/subscription/" + subscription1.getId())
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/usage/subscription/" + subscription2.getId())
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 3. OPTIMIZATION ANALYZER & LIFECYCLE TESTS
    // ==========================================

    @Test
    @DisplayName("Scenario 12: POST /api/optimization/analyze - Analyzes usage and generates REDUCE_LICENSES recommendation")
    void scenario12_analyzeUsage_GeneratesInsight() throws Exception {
        UsageRecord record = usageRecordRepository.save(UsageRecord.builder()
                .customer(customer1)
                .subscription(subscription1)
                .resourceType("LICENSE")
                .resourceName("Microsoft 365 Licenses")
                .allocatedQuantity(50)
                .usedQuantity(32)
                .unitCost(new BigDecimal("2000.00"))
                .snapshotDate(LocalDate.of(2026, 10, 31))
                .build());

        AnalyzeUsageRequest request = AnalyzeUsageRequest.builder()
                .usageRecordId(record.getId())
                .build();

        mockMvc.perform(post("/api/optimization/analyze")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].resourceName", is("Microsoft 365 Licenses")))
                .andExpect(jsonPath("$.data[0].allocatedQuantity", is(50)))
                .andExpect(jsonPath("$.data[0].usedQuantity", is(32)))
                .andExpect(jsonPath("$.data[0].unusedQuantity", is(18)))
                .andExpect(jsonPath("$.data[0].utilizationPercentage", is(64.00)))
                .andExpect(jsonPath("$.data[0].unitCost", is(2000.00)))
                .andExpect(jsonPath("$.data[0].potentialMonthlySavings", is(36000.00)))
                .andExpect(jsonPath("$.data[0].potentialAnnualSavings", is(432000.00)))
                .andExpect(jsonPath("$.data[0].recommendation", is("REDUCE_LICENSES")))
                .andExpect(jsonPath("$.data[0].status", is("DETECTED")));
    }

    @Test
    @DisplayName("Scenario 13: Re-running analysis for same usage snapshot returns existing insight without creating duplicate")
    void scenario13_duplicateAnalysisAvoidance() throws Exception {
        UsageRecord record = usageRecordRepository.save(UsageRecord.builder()
                .customer(customer1)
                .subscription(subscription1)
                .resourceType("LICENSE")
                .resourceName("Google Workspace Seats")
                .allocatedQuantity(100)
                .usedQuantity(80)
                .unitCost(new BigDecimal("1500.00"))
                .snapshotDate(LocalDate.now())
                .build());

        AnalyzeUsageRequest request = AnalyzeUsageRequest.builder()
                .usageRecordId(record.getId())
                .build();

        // Run analysis first time
        mockMvc.perform(post("/api/optimization/analyze")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        long countAfterFirst = optimizationInsightRepository.count();

        // Run analysis second time
        mockMvc.perform(post("/api/optimization/analyze")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        long countAfterSecond = optimizationInsightRepository.count();
        org.assertj.core.api.Assertions.assertThat(countAfterSecond).isEqualTo(countAfterFirst);
    }

    @Test
    @DisplayName("Scenario 14: POST /api/optimization/insights/{id}/preview - Preview returns exact Section 9 breakdown and transitions status to PREVIEWED")
    void scenario14_previewInsight_Success() throws Exception {
        UsageRecord record = usageRecordRepository.save(UsageRecord.builder()
                .customer(customer1)
                .subscription(subscription1)
                .resourceType("LICENSE")
                .resourceName("Microsoft 365 Licenses")
                .allocatedQuantity(50)
                .usedQuantity(32)
                .unitCost(new BigDecimal("2000.00"))
                .snapshotDate(LocalDate.now())
                .build());

        OptimizationInsight insight = optimizationInsightRepository.save(OptimizationInsight.builder()
                .customer(customer1)
                .subscription(subscription1)
                .usageRecord(record)
                .resourceName("Microsoft 365 Licenses")
                .allocatedQuantity(50)
                .usedQuantity(32)
                .unusedQuantity(18)
                .utilizationPercentage(new BigDecimal("64.00"))
                .unitCost(new BigDecimal("2000.00"))
                .potentialMonthlySavings(new BigDecimal("36000.00"))
                .potentialAnnualSavings(new BigDecimal("432000.00"))
                .status(OptimizationStatus.DETECTED)
                .recommendation(OptimizationRecommendation.REDUCE_LICENSES)
                .build());

        mockMvc.perform(post("/api/optimization/insights/" + insight.getId() + "/preview")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resourceName", is("Microsoft 365 Licenses")))
                .andExpect(jsonPath("$.data.allocatedQuantity", is(50)))
                .andExpect(jsonPath("$.data.usedQuantity", is(32)))
                .andExpect(jsonPath("$.data.unusedQuantity", is(18)))
                .andExpect(jsonPath("$.data.utilizationPercentage", is(64.00)))
                .andExpect(jsonPath("$.data.unitCost", is(2000.00)))
                .andExpect(jsonPath("$.data.currentMonthlyCost", is(100000.00)))
                .andExpect(jsonPath("$.data.potentialMonthlySavings", is(36000.00)))
                .andExpect(jsonPath("$.data.potentialAnnualSavings", is(432000.00)))
                .andExpect(jsonPath("$.data.recommendation", is("REDUCE_LICENSES")))
                .andExpect(jsonPath("$.data.status", is("PREVIEWED")))
                .andExpect(jsonPath("$.data.proposedAction", containsString("Reduce 18 unassigned Microsoft 365 Licenses")));
    }

    @Test
    @DisplayName("Scenario 15: POST /api/optimization/insights/{id}/approve - ADMIN approves; does NOT mutate subscription quantity or price")
    void scenario15_approveInsight_DoesNotMutateSubscription() throws Exception {
        UsageRecord record = usageRecordRepository.save(UsageRecord.builder()
                .customer(customer1)
                .subscription(subscription1)
                .resourceType("LICENSE")
                .resourceName("Microsoft 365 Licenses")
                .allocatedQuantity(50)
                .usedQuantity(32)
                .unitCost(new BigDecimal("2000.00"))
                .snapshotDate(LocalDate.now())
                .build());

        OptimizationInsight insight = optimizationInsightRepository.save(OptimizationInsight.builder()
                .customer(customer1)
                .subscription(subscription1)
                .usageRecord(record)
                .resourceName("Microsoft 365 Licenses")
                .allocatedQuantity(50)
                .usedQuantity(32)
                .unusedQuantity(18)
                .utilizationPercentage(new BigDecimal("64.00"))
                .unitCost(new BigDecimal("2000.00"))
                .potentialMonthlySavings(new BigDecimal("36000.00"))
                .potentialAnnualSavings(new BigDecimal("432000.00"))
                .status(OptimizationStatus.PREVIEWED)
                .recommendation(OptimizationRecommendation.REDUCE_LICENSES)
                .build());

        mockMvc.perform(post("/api/optimization/insights/" + insight.getId() + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("APPROVED")));

        // Verify Subscription was NOT mutated (Section 8 constraint)
        Subscription subscriptionAfterApproval = subscriptionRepository.findById(subscription1.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(subscriptionAfterApproval.getQuantity()).isEqualTo(50);
        org.assertj.core.api.Assertions.assertThat(subscriptionAfterApproval.getUnitPrice()).isEqualByComparingTo(new BigDecimal("2000.00"));
        org.assertj.core.api.Assertions.assertThat(subscriptionAfterApproval.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    @DisplayName("Scenario 16: Approval RBAC - STAFF and CUSTOMER forbidden from approving insights (403)")
    void scenario16_approvalRbac_ForbiddenForNonAdmin() throws Exception {
        UsageRecord record = usageRecordRepository.save(UsageRecord.builder()
                .customer(customer1)
                .subscription(subscription1)
                .resourceType("LICENSE")
                .resourceName("Licenses")
                .allocatedQuantity(20)
                .usedQuantity(10)
                .snapshotDate(LocalDate.now())
                .build());

        OptimizationInsight insight = optimizationInsightRepository.save(OptimizationInsight.builder()
                .customer(customer1)
                .subscription(subscription1)
                .usageRecord(record)
                .resourceName("Licenses")
                .allocatedQuantity(20)
                .usedQuantity(10)
                .unusedQuantity(10)
                .status(OptimizationStatus.PREVIEWED)
                .recommendation(OptimizationRecommendation.REDUCE_LICENSES)
                .build());

        // STAFF cannot approve -> 403
        mockMvc.perform(post("/api/optimization/insights/" + insight.getId() + "/approve")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());

        // CUSTOMER cannot approve -> 403
        mockMvc.perform(post("/api/optimization/insights/" + insight.getId() + "/approve")
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Scenario 17: POST /api/optimization/insights/{id}/reject - ADMIN rejects insight with reason")
    void scenario17_rejectInsight_Success() throws Exception {
        UsageRecord record = usageRecordRepository.save(UsageRecord.builder()
                .customer(customer1)
                .subscription(subscription1)
                .resourceType("LICENSE")
                .resourceName("Licenses")
                .allocatedQuantity(20)
                .usedQuantity(10)
                .snapshotDate(LocalDate.now())
                .build());

        OptimizationInsight insight = optimizationInsightRepository.save(OptimizationInsight.builder()
                .customer(customer1)
                .subscription(subscription1)
                .usageRecord(record)
                .resourceName("Licenses")
                .allocatedQuantity(20)
                .usedQuantity(10)
                .unusedQuantity(10)
                .status(OptimizationStatus.PREVIEWED)
                .recommendation(OptimizationRecommendation.REDUCE_LICENSES)
                .build());

        mockMvc.perform(post("/api/optimization/insights/" + insight.getId() + "/reject")
                        .param("reason", "Planned team expansion")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("REJECTED")))
                .andExpect(jsonPath("$.data.notes", containsString("Planned team expansion")));
    }

    @Test
    @DisplayName("Scenario 18: GET /api/optimization/history - Lists approved and rejected insights")
    void scenario18_historyQuery() throws Exception {
        UsageRecord record1 = usageRecordRepository.save(UsageRecord.builder()
                .customer(customer1)
                .subscription(subscription1)
                .resourceType("LICENSE")
                .resourceName("Resource 1")
                .snapshotDate(LocalDate.now())
                .build());

        usageRecordRepository.save(UsageRecord.builder()
                .customer(customer2)
                .subscription(subscription2)
                .resourceType("LICENSE")
                .resourceName("Resource 2")
                .snapshotDate(LocalDate.now())
                .build());

        optimizationInsightRepository.save(OptimizationInsight.builder()
                .customer(customer1)
                .subscription(subscription1)
                .usageRecord(record1)
                .resourceName("Resource 1")
                .status(OptimizationStatus.APPROVED)
                .recommendation(OptimizationRecommendation.REDUCE_LICENSES)
                .build());

        mockMvc.perform(get("/api/optimization/history")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", notNullValue()));
    }

    @Test
    @DisplayName("Scenario 19: Unauthenticated requests are rejected with 401 Unauthorized")
    void scenario19_unauthenticatedAccess_Rejected() throws Exception {
        mockMvc.perform(get("/api/usage"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/optimization/insights"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/optimization/analyze"))
                .andExpect(status().isUnauthorized());
    }
}

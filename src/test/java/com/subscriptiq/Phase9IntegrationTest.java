package com.subscriptiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscriptiq.ai.action.dto.ActionApprovalRequest;
import com.subscriptiq.ai.action.dto.ActionExecutionRequest;
import com.subscriptiq.ai.action.dto.ActionPreviewRequest;
import com.subscriptiq.ai.action.dto.ActionRejectionRequest;
import com.subscriptiq.ai.action.entity.AiActionAuditLog;
import com.subscriptiq.ai.action.entity.AiActionExecution;
import com.subscriptiq.ai.action.entity.AiActionRequest;
import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
import com.subscriptiq.ai.action.enums.AiActionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.action.repository.AiActionAuditLogRepository;
import com.subscriptiq.ai.action.repository.AiActionExecutionRepository;
import com.subscriptiq.ai.action.repository.AiActionRequestRepository;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.ai.enums.AiInsightStatus;
import com.subscriptiq.ai.enums.AiRecommendedAction;
import com.subscriptiq.ai.enums.AiSeverity;
import com.subscriptiq.ai.repository.AiInsightRepository;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.enums.CustomerStatus;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.invoice.entity.Invoice;
import com.subscriptiq.invoice.enums.InvoiceStatus;
import com.subscriptiq.invoice.repository.InvoiceRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class Phase9IntegrationTest {

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
    private UsageRecordRepository usageRecordRepository;

    @Autowired
    private AiInsightRepository aiInsightRepository;

    @Autowired
    private AiActionRequestRepository actionRequestRepository;

    @Autowired
    private AiActionExecutionRepository executionRepository;

    @Autowired
    private AiActionAuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String staffToken;
    private String customerToken;

    private User adminUser;
    private Customer testCustomer;
    private Plan testPlan;

    @BeforeEach
    void setUp() {
        dataCleaner.cleanAll();

        // 1. Admin
        adminUser = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin9@test.com")
                .password(passwordEncoder.encode("Admin@1234"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
        adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), "ADMIN");

        // 2. Staff
        User staffUser = userRepository.save(User.builder()
                .name("Staff User")
                .email("staff9@test.com")
                .password(passwordEncoder.encode("Staff@1234"))
                .role(UserRole.STAFF)
                .status(UserStatus.ACTIVE)
                .build());
        staffToken = jwtService.generateToken(staffUser.getId(), staffUser.getEmail(), "STAFF");

        // 3. Customer
        User customerUser = userRepository.save(User.builder()
                .name("Customer User")
                .email("cust9@test.com")
                .password(passwordEncoder.encode("Cust@1234"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());
        customerToken = jwtService.generateToken(customerUser.getId(), customerUser.getEmail(), "CUSTOMER");

        testCustomer = customerRepository.save(Customer.builder()
                .companyName("Phase 9 Global Industries")
                .contactName("Alice Wonderland")
                .email("alice9@global.com")
                .phone("9876543210")
                .status(CustomerStatus.ACTIVE)
                .user(customerUser)
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Enterprise Cloud Suite")
                .description("Cloud software suite")
                .status(ProductStatus.ACTIVE)
                .build());

        ProductVariant variant = productVariantRepository.save(ProductVariant.builder()
                .product(product)
                .name("Gold Tier")
                .sku("SKU-ECS-GOLD")
                .status(VariantStatus.ACTIVE)
                .build());

        testPlan = planRepository.save(Plan.builder()
                .productVariant(variant)
                .name("Enterprise Annual")
                .price(new BigDecimal("12000.00"))
                .currency("USD")
                .billingCycle(BillingCycle.YEARLY)
                .status(PlanStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Scenario 1: Preview valid renewal campaign action calculates live metrics without mutation")
    void scenario1_previewValidRenewalAction() throws Exception {
        AiInsight insight = aiInsightRepository.save(AiInsight.builder()
                .type("EXPIRING_SUBSCRIPTIONS")
                .title("Expiring Subscriptions")
                .description("Subscriptions expiring soon")
                .severity(AiSeverity.WARNING)
                .status(AiInsightStatus.NEW)
                .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                .build());

        // Create active subscription expiring in 15 days
        subscriptionRepository.save(Subscription.builder()
                .customer(testCustomer)
                .plan(testPlan)
                .subscriptionNumber("SUB-PH9-001")
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusMonths(11))
                .endDate(LocalDate.now().plusDays(15))
                .nextBillingDate(LocalDate.now().plusDays(15))
                .unitPrice(new BigDecimal("12000.00"))
                .quantity(1)
                .totalAmount(new BigDecimal("12000.00"))
                .build());

        ActionPreviewRequest request = ActionPreviewRequest.builder()
                .insightId(insight.getId())
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .build();

        mockMvc.perform(post("/api/ai/actions/preview")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.actionId", notNullValue()))
                .andExpect(jsonPath("$.data.actionType", is("SEND_RENEWAL_CAMPAIGN")))
                .andExpect(jsonPath("$.data.affectedCustomers", is(1)))
                .andExpect(jsonPath("$.data.affectedSubscriptions", is(1)))
                .andExpect(jsonPath("$.data.potentialRevenue", is(12000.00)))
                .andExpect(jsonPath("$.data.requiresApproval", is(true)))
                .andExpect(jsonPath("$.data.status", is("PENDING_APPROVAL")));

        // Verify audit log recorded
        List<AiActionAuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getAction()).isEqualTo("PREVIEW_ACTION");
    }

    @Test
    @DisplayName("Scenario 2: Preview rejects incompatible action type vs insight recommendation")
    void scenario2_previewRejectsIncompatibleAction() throws Exception {
        AiInsight insight = aiInsightRepository.save(AiInsight.builder()
                .type("EXPIRING_SUBSCRIPTIONS")
                .title("Expiring Subscriptions")
                .severity(AiSeverity.INFO)
                .status(AiInsightStatus.NEW)
                .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                .build());

        ActionPreviewRequest request = ActionPreviewRequest.builder()
                .insightId(insight.getId())
                .actionType(AiActionType.FOLLOW_UP_OVERDUE_CUSTOMERS)
                .build();

        mockMvc.perform(post("/api/ai/actions/preview")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("does not match insight recommendation")));
    }

    @Test
    @DisplayName("Scenario 3: Preview rejects non-existent insight")
    void scenario3_previewRejectsNonExistentInsight() throws Exception {
        ActionPreviewRequest request = ActionPreviewRequest.builder()
                .insightId(99999L)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .build();

        mockMvc.perform(post("/api/ai/actions/preview")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("AI Insight not found")));
    }

    @Test
    @DisplayName("Scenario 4: Preview rejects already RESOLVED insight")
    void scenario4_previewRejectsResolvedInsight() throws Exception {
        AiInsight insight = aiInsightRepository.save(AiInsight.builder()
                .type("EXPIRING_SUBSCRIPTIONS")
                .title("Resolved Expiring Subscriptions")
                .severity(AiSeverity.INFO)
                .status(AiInsightStatus.RESOLVED)
                .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                .build());

        ActionPreviewRequest request = ActionPreviewRequest.builder()
                .insightId(insight.getId())
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .build();

        mockMvc.perform(post("/api/ai/actions/preview")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("cannot be acted upon")));
    }

    @Test
    @DisplayName("Scenario 5: Admin approves action request")
    void scenario5_adminApprovesAction() throws Exception {
        AiInsight insight = aiInsightRepository.save(AiInsight.builder()
                .type("EXPIRING_SUBSCRIPTIONS")
                .title("Expiring Subscriptions")
                .status(AiInsightStatus.NEW)
                .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                .build());

        AiActionRequest action = actionRequestRepository.save(AiActionRequest.builder()
                .insight(insight)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .status(AiActionStatus.PENDING_APPROVAL)
                .title("Send Renewal Campaign")
                .requiresApproval(true)
                .build());

        ActionApprovalRequest approvalRequest = ActionApprovalRequest.builder()
                .note("Approved by compliance team")
                .build();

        mockMvc.perform(post("/api/ai/actions/{actionId}/approve", action.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("APPROVED")))
                .andExpect(jsonPath("$.data.approvedBy", is("admin9@test.com")));

        // Verify action status in DB
        AiActionRequest updated = actionRequestRepository.findById(action.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(AiActionStatus.APPROVED);
        assertThat(updated.getApprovedBy()).isEqualTo("admin9@test.com");
    }

    @Test
    @DisplayName("Scenario 6: Admin rejects action request")
    void scenario6_adminRejectsAction() throws Exception {
        AiInsight insight = aiInsightRepository.save(AiInsight.builder()
                .type("EXPIRING_SUBSCRIPTIONS")
                .title("Expiring Subscriptions")
                .status(AiInsightStatus.NEW)
                .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                .build());

        AiActionRequest action = actionRequestRepository.save(AiActionRequest.builder()
                .insight(insight)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .status(AiActionStatus.PENDING_APPROVAL)
                .title("Send Renewal Campaign")
                .requiresApproval(true)
                .build());

        ActionRejectionRequest rejectRequest = ActionRejectionRequest.builder()
                .reason("Customer requested extension offline")
                .build();

        mockMvc.perform(post("/api/ai/actions/{actionId}/reject", action.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("REJECTED")))
                .andExpect(jsonPath("$.data.rejectedBy", is("admin9@test.com")))
                .andExpect(jsonPath("$.data.rejectionReason", is("Customer requested extension offline")));

        AiActionRequest updated = actionRequestRepository.findById(action.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(AiActionStatus.REJECTED);
    }

    @Test
    @DisplayName("Scenario 7: Execute approved renewal campaign action succeeds and updates insight")
    void scenario7_executeApprovedAction() throws Exception {
        AiInsight insight = aiInsightRepository.save(AiInsight.builder()
                .type("EXPIRING_SUBSCRIPTIONS")
                .title("Expiring Subscriptions")
                .status(AiInsightStatus.ACTION_PENDING)
                .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                .build());

        subscriptionRepository.save(Subscription.builder()
                .customer(testCustomer)
                .plan(testPlan)
                .subscriptionNumber("SUB-EXEC-001")
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusMonths(11))
                .endDate(LocalDate.now().plusDays(10))
                .nextBillingDate(LocalDate.now().plusDays(10))
                .unitPrice(new BigDecimal("12000.00"))
                .quantity(1)
                .totalAmount(new BigDecimal("12000.00"))
                .build());

        AiActionRequest action = actionRequestRepository.save(AiActionRequest.builder()
                .insight(insight)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .status(AiActionStatus.APPROVED)
                .title("Send Renewal Campaign")
                .approvedBy("admin9@test.com")
                .requiresApproval(true)
                .build());

        mockMvc.perform(post("/api/ai/actions/{actionId}/execute", action.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("SUCCESS")))
                .andExpect(jsonPath("$.data.affectedCount", is(1)))
                .andExpect(jsonPath("$.data.previouslyExecuted", is(false)));

        // Verify action status in DB
        AiActionRequest updatedAction = actionRequestRepository.findById(action.getId()).orElseThrow();
        assertThat(updatedAction.getStatus()).isEqualTo(AiActionStatus.EXECUTED);

        // Verify insight transitioned to RESOLVED
        AiInsight updatedInsight = aiInsightRepository.findById(insight.getId()).orElseThrow();
        assertThat(updatedInsight.getStatus()).isEqualTo(AiInsightStatus.RESOLVED);

        // Verify execution history record created
        List<AiActionExecution> execs = executionRepository.findByActionId(action.getId());
        assertThat(execs).hasSize(1);
        assertThat(execs.get(0).getStatus()).isEqualTo(AiActionExecutionStatus.SUCCESS);
    }

    @Test
    @DisplayName("Scenario 8: Cannot execute unapproved action in PENDING_APPROVAL status")
    void scenario8_cannotExecuteUnapprovedAction() throws Exception {
        AiInsight insight = aiInsightRepository.save(AiInsight.builder()
                .type("EXPIRING_SUBSCRIPTIONS")
                .title("Expiring Subscriptions")
                .status(AiInsightStatus.NEW)
                .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                .build());

        AiActionRequest action = actionRequestRepository.save(AiActionRequest.builder()
                .insight(insight)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .status(AiActionStatus.PENDING_APPROVAL)
                .title("Unapproved Action")
                .build());

        mockMvc.perform(post("/api/ai/actions/{actionId}/execute", action.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("must be approved by an administrator before execution")));
    }

    @Test
    @DisplayName("Scenario 9: Cannot execute REJECTED action")
    void scenario9_cannotExecuteRejectedAction() throws Exception {
        AiInsight insight = aiInsightRepository.save(AiInsight.builder()
                .type("EXPIRING_SUBSCRIPTIONS")
                .title("Expiring Subscriptions")
                .status(AiInsightStatus.NEW)
                .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                .build());

        AiActionRequest action = actionRequestRepository.save(AiActionRequest.builder()
                .insight(insight)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .status(AiActionStatus.REJECTED)
                .title("Rejected Action")
                .build());

        mockMvc.perform(post("/api/ai/actions/{actionId}/execute", action.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("because it has been REJECTED")));
    }

    @Test
    @DisplayName("Scenario 10: Idempotency - Executing an already EXECUTED action returns previous result without duplicate run")
    void scenario10_idempotencyOnExecutedAction() throws Exception {
        AiInsight insight = aiInsightRepository.save(AiInsight.builder()
                .type("EXPIRING_SUBSCRIPTIONS")
                .title("Expiring Subscriptions")
                .status(AiInsightStatus.RESOLVED)
                .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                .build());

        AiActionRequest action = actionRequestRepository.save(AiActionRequest.builder()
                .insight(insight)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .status(AiActionStatus.EXECUTED)
                .title("Already Executed Action")
                .build());

        executionRepository.save(AiActionExecution.builder()
                .action(action)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .executedBy("admin9@test.com")
                .status(AiActionExecutionStatus.SUCCESS)
                .affectedCount(5)
                .resultSummary("Renewal campaign dispatched for 5 customers")
                .build());

        mockMvc.perform(post("/api/ai/actions/{actionId}/execute", action.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.previouslyExecuted", is(true)))
                .andExpect(jsonPath("$.data.affectedCount", is(5)))
                .andExpect(jsonPath("$.data.resultSummary", containsString("Renewal campaign dispatched for 5 customers")));

        // Ensure no duplicate execution was inserted
        List<AiActionExecution> executions = executionRepository.findByActionId(action.getId());
        assertThat(executions).hasSize(1);
    }

    @Test
    @DisplayName("Scenario 11: Overdue customer follow-up executor does not mutate invoices or balances")
    void scenario11_overdueFollowUpExecutionDoesNotMutateInvoices() throws Exception {
        Invoice overdueInv = invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-PH9-OVERDUE")
                .customer(testCustomer)
                .status(InvoiceStatus.OVERDUE)
                .totalAmount(new BigDecimal("750.00"))
                .balanceAmount(new BigDecimal("750.00"))
                .dueDate(LocalDate.now().minusDays(10))
                .invoiceDate(LocalDate.now().minusDays(40))
                .build());

        AiInsight insight = aiInsightRepository.save(AiInsight.builder()
                .type("OVERDUE_PAYMENTS")
                .title("Overdue Accounts")
                .status(AiInsightStatus.NEW)
                .recommendedAction(AiRecommendedAction.FOLLOW_UP_OVERDUE_CUSTOMERS)
                .build());

        AiActionRequest action = actionRequestRepository.save(AiActionRequest.builder()
                .insight(insight)
                .actionType(AiActionType.FOLLOW_UP_OVERDUE_CUSTOMERS)
                .status(AiActionStatus.APPROVED)
                .title("Follow Up Overdue Customers")
                .requiresApproval(true)
                .build());

        mockMvc.perform(post("/api/ai/actions/{actionId}/execute", action.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("SUCCESS")))
                .andExpect(jsonPath("$.data.affectedCount", is(1)));

        // Verify invoice was not mutated
        Invoice reloaded = invoiceRepository.findById(overdueInv.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(InvoiceStatus.OVERDUE);
        assertThat(reloaded.getBalanceAmount()).isEqualByComparingTo("750.00");
    }

    @Test
    @DisplayName("Scenario 12: Unused resource review does not alter subscription quantities")
    void scenario12_unusedResourceExecutionDoesNotMutateSubscriptionQuantity() throws Exception {
        Subscription sub = subscriptionRepository.save(Subscription.builder()
                .customer(testCustomer)
                .plan(testPlan)
                .subscriptionNumber("SUB-USAGE-001")
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusMonths(2))
                .endDate(LocalDate.now().plusMonths(10))
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .unitPrice(new BigDecimal("12000.00"))
                .quantity(1)
                .totalAmount(new BigDecimal("12000.00"))
                .build());

        usageRecordRepository.save(UsageRecord.builder()
                .customer(testCustomer)
                .subscription(sub)
                .resourceName("Compute Core")
                .resourceType("CPU")
                .allocatedQuantity(50)
                .usedQuantity(20)
                .unitCost(new BigDecimal("100.00"))
                .snapshotDate(LocalDate.now())
                .build());

        AiInsight insight = aiInsightRepository.save(AiInsight.builder()
                .type("UNUSED_RESOURCES")
                .title("Idle Resources")
                .status(AiInsightStatus.NEW)
                .recommendedAction(AiRecommendedAction.REVIEW_UNUSED_RESOURCES)
                .build());

        AiActionRequest action = actionRequestRepository.save(AiActionRequest.builder()
                .insight(insight)
                .actionType(AiActionType.REVIEW_UNUSED_RESOURCES)
                .status(AiActionStatus.APPROVED)
                .title("Review Idle Resources")
                .build());

        mockMvc.perform(post("/api/ai/actions/{actionId}/execute", action.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.resultSummary", containsString("Manual account review required before quantity modifications")));
    }

    @Test
    @DisplayName("Scenario 13: List and get action requests")
    void scenario13_listAndGetActionRequests() throws Exception {
        AiInsight insight = aiInsightRepository.save(AiInsight.builder()
                .type("EXPIRING_SUBSCRIPTIONS")
                .title("Expiring Subscriptions")
                .status(AiInsightStatus.NEW)
                .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                .build());

        AiActionRequest action = actionRequestRepository.save(AiActionRequest.builder()
                .insight(insight)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .status(AiActionStatus.PENDING_APPROVAL)
                .title("Renewal Campaign")
                .build());

        // GET /api/ai/actions
        mockMvc.perform(get("/api/ai/actions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content[0].id", is(action.getId().intValue())));

        // GET /api/ai/actions/{id}
        mockMvc.perform(get("/api/ai/actions/{actionId}", action.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(action.getId().intValue())));
    }

    @Test
    @DisplayName("Scenario 14: List action execution history")
    void scenario14_listActionHistory() throws Exception {
        AiInsight insight = aiInsightRepository.save(AiInsight.builder()
                .type("EXPIRING_SUBSCRIPTIONS")
                .title("Expiring Subscriptions")
                .status(AiInsightStatus.RESOLVED)
                .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                .build());

        AiActionRequest action = actionRequestRepository.save(AiActionRequest.builder()
                .insight(insight)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .status(AiActionStatus.EXECUTED)
                .title("Campaign Action")
                .build());

        executionRepository.save(AiActionExecution.builder()
                .action(action)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .executedBy("admin9@test.com")
                .status(AiActionExecutionStatus.SUCCESS)
                .affectedCount(3)
                .resultSummary("Campaign executed for 3 accounts")
                .build());

        mockMvc.perform(get("/api/ai/actions/history")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content[0].affectedCount", is(3)));
    }

    @Test
    @DisplayName("Scenario 15: Security & RBAC enforcement - Non-admin roles receive 403, unauthenticated 401")
    void scenario15_securityRbacEnforcement() throws Exception {
        ActionPreviewRequest request = ActionPreviewRequest.builder()
                .insightId(1L)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .build();

        // 1. STAFF forbidden
        mockMvc.perform(post("/api/ai/actions/preview")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // 2. CUSTOMER forbidden
        mockMvc.perform(post("/api/ai/actions/preview")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // 3. Unauthenticated unauthorized
        mockMvc.perform(post("/api/ai/actions/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Scenario 16: Security - Arbitrary SQL and instructions injected into payload are ignored and cannot execute")
    void scenario16_cannotInjectArbitrarySqlOrCommands() throws Exception {
        AiInsight insight = aiInsightRepository.save(AiInsight.builder()
                .type("EXPIRING_SUBSCRIPTIONS")
                .title("Expiring Subscriptions")
                .status(AiInsightStatus.NEW)
                .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                .build());

        String maliciousPayload = """
                {
                    "insightId": %d,
                    "actionType": "SEND_RENEWAL_CAMPAIGN",
                    "sql": "DROP TABLE users;",
                    "command": "rm -rf /",
                    "affectedCustomers": 99999,
                    "potentialRevenue": 99999999.00
                }
                """.formatted(insight.getId());

        mockMvc.perform(post("/api/ai/actions/preview")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(maliciousPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                // Verify server ignored malicious client injected counts and used real DB values
                .andExpect(jsonPath("$.data.affectedCustomers", is(0)))
                .andExpect(jsonPath("$.data.potentialRevenue", is(0)));
    }
}

package com.subscriptiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscriptiq.customer.dto.CreateCustomerRequest;
import com.subscriptiq.customer.dto.UpdateCustomerRequest;
import com.subscriptiq.customer.dto.UpdateCustomerStatusRequest;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.enums.CustomerStatus;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.plan.dto.CreatePlanRequest;
import com.subscriptiq.plan.dto.UpdatePlanRequest;
import com.subscriptiq.plan.dto.UpdatePlanStatusRequest;
import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.enums.BillingCycle;
import com.subscriptiq.plan.enums.PlanStatus;
import com.subscriptiq.plan.repository.PlanRepository;
import com.subscriptiq.product.dto.CreateProductRequest;
import com.subscriptiq.product.dto.CreateVariantRequest;
import com.subscriptiq.product.dto.UpdateProductRequest;
import com.subscriptiq.product.dto.UpdateProductStatusRequest;
import com.subscriptiq.product.dto.UpdateVariantRequest;
import com.subscriptiq.product.dto.UpdateVariantStatusRequest;
import com.subscriptiq.product.entity.Product;
import com.subscriptiq.product.entity.ProductVariant;
import com.subscriptiq.product.enums.ProductStatus;
import com.subscriptiq.product.enums.VariantStatus;
import com.subscriptiq.product.repository.ProductRepository;
import com.subscriptiq.product.repository.ProductVariantRepository;
import com.subscriptiq.quotation.repository.QuotationRepository;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import com.subscriptiq.security.JwtService;
import com.subscriptiq.user.entity.User;
import com.subscriptiq.user.enums.UserRole;
import com.subscriptiq.user.enums.UserStatus;
import com.subscriptiq.user.repository.PasswordResetTokenRepository;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class Phase3IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private com.subscriptiq.payment.repository.PaymentRepository paymentRepository;

    @Autowired
    private com.subscriptiq.invoice.repository.InvoiceRepository invoiceRepository;

    @Autowired
    private TestDataCleaner dataCleaner;

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

    @BeforeEach
    void setUp() {
        dataCleaner.cleanAll();

        adminUser = userRepository.save(User.builder()
                .name("Admin")
                .email("admin@test.com")
                .password(passwordEncoder.encode("Pass@123"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        staffUser = userRepository.save(User.builder()
                .name("Staff")
                .email("staff@test.com")
                .password(passwordEncoder.encode("Pass@123"))
                .role(UserRole.STAFF)
                .status(UserStatus.ACTIVE)
                .build());

        customerUser1 = userRepository.save(User.builder()
                .name("Customer 1")
                .email("cust1@test.com")
                .password(passwordEncoder.encode("Pass@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());

        customerUser2 = userRepository.save(User.builder()
                .name("Customer 2")
                .email("cust2@test.com")
                .password(passwordEncoder.encode("Pass@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());

        adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), "ADMIN");
        staffToken = jwtService.generateToken(staffUser.getId(), staffUser.getEmail(), "STAFF");
        customer1Token = jwtService.generateToken(customerUser1.getId(), customerUser1.getEmail(), "CUSTOMER");
        customer2Token = jwtService.generateToken(customerUser2.getId(), customerUser2.getEmail(), "CUSTOMER");
    }

    // ==========================================
    // 1. CUSTOMER INTEGRATION TESTS
    // ==========================================

    @Test
    @DisplayName("POST /api/customers - STAFF can create customer successfully")
    void testStaffCreateCustomer_Success() throws Exception {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
                .companyName("Acme Global")
                .contactName("Wile E. Coyote")
                .email("wile@acme.com")
                .phone("555-1234")
                .city("Phoenix")
                .state("AZ")
                .country("USA")
                .build();

        mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.companyName", is("Acme Global")))
                .andExpect(jsonPath("$.data.email", is("wile@acme.com")))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("POST /api/customers - CUSTOMER cannot create customer (403 Forbidden)")
    void testCustomerCreateCustomer_Forbidden() throws Exception {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
                .companyName("Illegal Inc")
                .contactName("Hacker")
                .email("hacker@illegal.com")
                .build();

        mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + customer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/customers - ADMIN/STAFF can list and search customers")
    void testListCustomers_Success() throws Exception {
        customerRepository.save(Customer.builder()
                .companyName("Acme Corp")
                .contactName("Alice")
                .email("alice@acme.com")
                .status(CustomerStatus.ACTIVE)
                .build());

        customerRepository.save(Customer.builder()
                .companyName("Beta LLC")
                .contactName("Bob")
                .email("bob@beta.com")
                .status(CustomerStatus.ACTIVE)
                .build());

        // Search for "acme"
        mockMvc.perform(get("/api/customers?search=acme")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content.length()", is(1)))
                .andExpect(jsonPath("$.data.content[0].companyName", is("Acme Corp")));
    }

    @Test
    @DisplayName("GET /api/customers - CUSTOMER cannot list all customers (403 Forbidden)")
    void testCustomerListAllCustomers_Forbidden() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/customers/{id} - CUSTOMER can view own record, forbidden from others")
    void testCustomerViewOwnRecordOnly() throws Exception {
        Customer c1 = customerRepository.save(Customer.builder()
                .user(customerUser1)
                .companyName("Customer 1 Corp")
                .contactName("Cust 1")
                .email("cust1@corp.com")
                .status(CustomerStatus.ACTIVE)
                .build());

        Customer c2 = customerRepository.save(Customer.builder()
                .user(customerUser2)
                .companyName("Customer 2 Corp")
                .contactName("Cust 2")
                .email("cust2@corp.com")
                .status(CustomerStatus.ACTIVE)
                .build());

        // Customer 1 accessing own record -> 200 OK
        mockMvc.perform(get("/api/customers/" + c1.getId())
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName", is("Customer 1 Corp")));

        // Customer 1 accessing Customer 2's record -> 401 Unauthorized / Forbidden
        mockMvc.perform(get("/api/customers/" + c2.getId())
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/customers/{id} - Update customer profile")
    void testUpdateCustomer_Success() throws Exception {
        Customer customer = customerRepository.save(Customer.builder()
                .companyName("Old Name")
                .contactName("Old Contact")
                .email("old@example.com")
                .status(CustomerStatus.ACTIVE)
                .build());

        UpdateCustomerRequest updateReq = UpdateCustomerRequest.builder()
                .companyName("Updated Name")
                .contactName("New Contact")
                .email("updated@example.com")
                .build();

        mockMvc.perform(put("/api/customers/" + customer.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName", is("Updated Name")));
    }

    @Test
    @DisplayName("PATCH /api/customers/{id}/status - Deactivate customer")
    void testDeactivateCustomer_Success() throws Exception {
        Customer customer = customerRepository.save(Customer.builder()
                .companyName("Deactivate Test")
                .contactName("Contact")
                .email("deact@example.com")
                .status(CustomerStatus.ACTIVE)
                .build());

        UpdateCustomerStatusRequest request = new UpdateCustomerStatusRequest(CustomerStatus.INACTIVE);

        mockMvc.perform(patch("/api/customers/" + customer.getId() + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("INACTIVE")));
    }

    // ==========================================
    // 2. PRODUCT INTEGRATION TESTS
    // ==========================================

    @Test
    @DisplayName("POST /api/products - ADMIN/STAFF can create product")
    void testCreateProduct_Success() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("SubscriptIQ Core")
                .description("Core subscription management system")
                .category("Billing")
                .build();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("SubscriptIQ Core")))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("POST /api/products - CUSTOMER cannot create product (403 Forbidden)")
    void testCustomerCreateProduct_Forbidden() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Hacker Product")
                .build();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + customer1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/products - CUSTOMER can view active products, inactive hidden")
    void testCustomerViewActiveProducts() throws Exception {
        productRepository.save(Product.builder()
                .name("Active SaaS")
                .status(ProductStatus.ACTIVE)
                .build());

        productRepository.save(Product.builder()
                .name("Deprecated SaaS")
                .status(ProductStatus.INACTIVE)
                .build());

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + customer1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()", is(1)))
                .andExpect(jsonPath("$.data.content[0].name", is("Active SaaS")));
    }

    // ==========================================
    // 3. PRODUCT VARIANT INTEGRATION TESTS
    // ==========================================

    @Test
    @DisplayName("POST /api/products/{productId}/variants - Create variant under product")
    void testCreateVariant_Success() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Platform")
                .status(ProductStatus.ACTIVE)
                .build());

        CreateVariantRequest request = CreateVariantRequest.builder()
                .name("Standard Edition")
                .sku("PLT-STD-001")
                .description("Standard edition")
                .build();

        mockMvc.perform(post("/api/products/" + product.getId() + "/variants")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("Standard Edition")))
                .andExpect(jsonPath("$.data.sku", is("PLT-STD-001")));
    }

    @Test
    @DisplayName("POST /api/products/{productId}/variants - Duplicate SKU returns 409 Conflict")
    void testCreateVariant_DuplicateSku() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Platform")
                .status(ProductStatus.ACTIVE)
                .build());

        variantRepository.save(ProductVariant.builder()
                .product(product)
                .name("Existing")
                .sku("DUPLICATE-SKU")
                .status(VariantStatus.ACTIVE)
                .build());

        CreateVariantRequest request = CreateVariantRequest.builder()
                .name("New Variant")
                .sku("DUPLICATE-SKU")
                .build();

        mockMvc.perform(post("/api/products/" + product.getId() + "/variants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    // ==========================================
    // 4. PLAN INTEGRATION TESTS
    // ==========================================

    @Test
    @DisplayName("POST /api/variants/{variantId}/plans - Create plan with BigDecimal price & currency")
    void testCreatePlan_Success() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Platform")
                .status(ProductStatus.ACTIVE)
                .build());

        ProductVariant variant = variantRepository.save(ProductVariant.builder()
                .product(product)
                .name("Pro Edition")
                .sku("PLT-PRO-001")
                .status(VariantStatus.ACTIVE)
                .build());

        CreatePlanRequest request = CreatePlanRequest.builder()
                .name("Pro Monthly")
                .description("Monthly plan for Pro Edition")
                .billingCycle(BillingCycle.MONTHLY)
                .price(new BigDecimal("1499.50"))
                .currency("INR")
                .trialDays(14)
                .build();

        mockMvc.perform(post("/api/variants/" + variant.getId() + "/plans")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("Pro Monthly")))
                .andExpect(jsonPath("$.data.price", is(1499.50)))
                .andExpect(jsonPath("$.data.currency", is("INR")))
                .andExpect(jsonPath("$.data.trialDays", is(14)))
                .andExpect(jsonPath("$.data.billingCycle", is("MONTHLY")));
    }

    @Test
    @DisplayName("POST /api/variants/{variantId}/plans - Negative price rejected by validation")
    void testCreatePlan_NegativePrice_Rejected() throws Exception {
        Product product = productRepository.save(Product.builder().name("P").status(ProductStatus.ACTIVE).build());
        ProductVariant variant = variantRepository.save(ProductVariant.builder().product(product).name("V").sku("V-1").status(VariantStatus.ACTIVE).build());

        CreatePlanRequest request = CreatePlanRequest.builder()
                .name("Invalid Plan")
                .billingCycle(BillingCycle.MONTHLY)
                .price(new BigDecimal("-10.00"))
                .build();

        mockMvc.perform(post("/api/variants/" + variant.getId() + "/plans")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.price", notNullValue()));
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoints returns 401")
    void testUnauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }
}


package com.subscriptiq.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscriptiq.TestDataCleaner;
import com.subscriptiq.user.dto.CreateStaffRequest;
import com.subscriptiq.user.dto.UpdateUserStatusRequest;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.subscriptiq.SubscriptIqApplication.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SecurityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    @Autowired
    private com.subscriptiq.customer.repository.CustomerRepository customerRepository;

    @Autowired
    private com.subscriptiq.quotation.repository.QuotationRepository quotationRepository;

    @Autowired
    private com.subscriptiq.subscription.repository.SubscriptionRepository subscriptionRepository;

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
    private User customerUser;

    private String adminToken;
    private String staffToken;
    private String customerToken;

    @BeforeEach
    void setUp() {
        dataCleaner.cleanAll();

        adminUser = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@subscriptiq.com")
                .password(passwordEncoder.encode("AdminPass@123"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        staffUser = userRepository.save(User.builder()
                .name("Staff Member")
                .email("staff@subscriptiq.com")
                .password(passwordEncoder.encode("StaffPass@123"))
                .role(UserRole.STAFF)
                .status(UserStatus.ACTIVE)
                .build());

        customerUser = userRepository.save(User.builder()
                .name("Customer One")
                .email("customer@example.com")
                .password(passwordEncoder.encode("CustPass@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());

        adminToken = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), "ADMIN");
        staffToken = jwtService.generateToken(staffUser.getId(), staffUser.getEmail(), "STAFF");
        customerToken = jwtService.generateToken(customerUser.getId(), customerUser.getEmail(), "CUSTOMER");
    }

    @Test
    @DisplayName("GET /api/test/me - Unauthenticated request rejected with 401")
    void testMe_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/test/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("GET /api/test/me - Invalid JWT rejected with 401")
    void testMe_InvalidJwt() throws Exception {
        mockMvc.perform(get("/api/test/me")
                        .header("Authorization", "Bearer invalid.token.payload"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("GET /api/test/me - Authenticated request returns user profile")
    void testMe_Authenticated() throws Exception {
        mockMvc.perform(get("/api/test/me")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.userId", is(customerUser.getId().intValue())))
                .andExpect(jsonPath("$.data.email", is("customer@example.com")))
                .andExpect(jsonPath("$.data.role", is("CUSTOMER")));
    }

    @Test
    @DisplayName("GET /api/test/admin - ADMIN can access")
    void testAdminEndpoint_AdminAccess() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("GET /api/test/admin - STAFF rejected with 403 Forbidden")
    void testAdminEndpoint_StaffForbidden() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("GET /api/test/admin - CUSTOMER rejected with 403 Forbidden")
    void testAdminEndpoint_CustomerForbidden() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("GET /api/test/staff - ADMIN and STAFF can access")
    void testStaffEndpoint_AdminAndStaffAccess() throws Exception {
        // Staff access
        mockMvc.perform(get("/api/test/staff")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());

        // Admin access
        mockMvc.perform(get("/api/test/staff")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/test/staff - CUSTOMER rejected with 403 Forbidden")
    void testStaffEndpoint_CustomerForbidden() throws Exception {
        mockMvc.perform(get("/api/test/staff")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("GET /api/test/customer - CUSTOMER can access")
    void testCustomerEndpoint_CustomerAccess() throws Exception {
        mockMvc.perform(get("/api/test/customer")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("POST /api/admin/staff - Only ADMIN can create staff")
    void testAdminCreateStaff_Success() throws Exception {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .name("New Staff Person")
                .email("newstaff@subscriptiq.com")
                .password("StaffPassword@123")
                .build();

        mockMvc.perform(post("/api/admin/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.role", is("STAFF")))
                .andExpect(jsonPath("$.data.email", is("newstaff@subscriptiq.com")));
    }

    @Test
    @DisplayName("POST /api/admin/staff - Non-admin cannot create staff (403 Forbidden)")
    void testNonAdminCreateStaff_Forbidden() throws Exception {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .name("Malicious Attempt")
                .email("malicious@subscriptiq.com")
                .password("StaffPassword@123")
                .build();

        // Customer attempt
        mockMvc.perform(post("/api/admin/staff")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // Staff attempt
        mockMvc.perform(post("/api/admin/staff")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/users - ADMIN can list all users")
    void testAdminListUsers_Success() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("PATCH /api/admin/users/{id}/status - ADMIN can update user status")
    void testAdminUpdateUserStatus_Success() throws Exception {
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.INACTIVE)
                .build();

        mockMvc.perform(patch("/api/admin/users/" + customerUser.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("INACTIVE")));
    }

    @Test
    @DisplayName("PATCH /api/admin/users/{id}/status - Rejects deactivating the only active admin")
    void testAdminCannotDeactivateLastAdmin() throws Exception {
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.INACTIVE)
                .build();

        mockMvc.perform(patch("/api/admin/users/" + adminUser.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("Cannot deactivate the last remaining active administrator")));
    }
}


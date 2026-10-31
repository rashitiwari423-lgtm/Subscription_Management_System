package com.subscriptiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscriptiq.auth.dto.LoginRequest;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.customer.enums.CustomerStatus;
import com.subscriptiq.customer.repository.CustomerRepository;
import com.subscriptiq.security.JwtService;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class Phase11SecurityAndHardeningTest {

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private com.subscriptiq.security.RateLimitFilter rateLimitFilter;

    private User activeAdmin;
    private User activeStaff;
    private User activeCustomer;
    private User inactiveUser;
    private Customer customer1;
    private Customer customer2;

    private String adminToken;
    private String staffToken;
    private String customerToken;

    @BeforeEach
    void setUp() {
        dataCleaner.cleanAll();
        rateLimitFilter.reset();

        activeAdmin = userRepository.save(User.builder()
                .name("Sec Admin")
                .email("admin11@sec.com")
                .password(passwordEncoder.encode("AdminSec@1234"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
        adminToken = jwtService.generateToken(activeAdmin.getId(), activeAdmin.getEmail(), "ADMIN");

        activeStaff = userRepository.save(User.builder()
                .name("Sec Staff")
                .email("staff11@sec.com")
                .password(passwordEncoder.encode("StaffSec@1234"))
                .role(UserRole.STAFF)
                .status(UserStatus.ACTIVE)
                .build());
        staffToken = jwtService.generateToken(activeStaff.getId(), activeStaff.getEmail(), "STAFF");

        activeCustomer = userRepository.save(User.builder()
                .name("Sec Customer 1")
                .email("cust11_1@sec.com")
                .password(passwordEncoder.encode("CustSec@1234"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());
        customerToken = jwtService.generateToken(activeCustomer.getId(), activeCustomer.getEmail(), "CUSTOMER");

        inactiveUser = userRepository.save(User.builder()
                .name("Inactive User")
                .email("inactive11@sec.com")
                .password(passwordEncoder.encode("Inactive@1234"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.INACTIVE)
                .build());

        customer1 = customerRepository.save(Customer.builder()
                .companyName("Secure Corp 1")
                .contactName("John Doe")
                .email("john11@sec1.com")
                .phone("1111111111")
                .status(CustomerStatus.ACTIVE)
                .user(activeCustomer)
                .build());

        User customerUser2 = userRepository.save(User.builder()
                .name("Sec Customer 2")
                .email("cust11_2@sec.com")
                .password(passwordEncoder.encode("CustSec@1234"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());

        customer2 = customerRepository.save(Customer.builder()
                .companyName("Secure Corp 2")
                .contactName("Jane Roe")
                .email("jane11@sec2.com")
                .phone("2222222222")
                .status(CustomerStatus.ACTIVE)
                .user(customerUser2)
                .build());
    }

    @Test
    @DisplayName("Security 1: Health check endpoint returns UP and does not leak credentials")
    void testHealthCheckSafe() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("UP")))
                .andExpect(jsonPath("$.data.service", containsString("SubscriptIQ")))
                .andExpect(jsonPath("$.data.version", is("1.0.0")))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.secret").doesNotExist())
                .andExpect(jsonPath("$.data.database").doesNotExist());
    }

    @Test
    @DisplayName("Security 2: Security headers are present on API responses")
    void testSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @DisplayName("Security 3: Malformed JWT token is rejected with HTTP 401")
    void testMalformedJwtRejected() throws Exception {
        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", "Bearer invalid.malformed.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security 4: Unauthenticated access to protected endpoints is rejected with HTTP 401")
    void testUnauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security 5: Inactive/disabled user is prevented from authenticating")
    void testInactiveUserCannotAuthenticate() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("inactive11@sec.com");
        loginRequest.setPassword("Inactive@1234");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("inactive")));
    }

    @Test
    @DisplayName("Security 6: Inactive user with validly signed token is rejected on request")
    void testInactiveUserWithTokenRejected() throws Exception {
        String tokenForInactive = jwtService.generateToken(inactiveUser.getId(), inactiveUser.getEmail(), "CUSTOMER");

        mockMvc.perform(get("/api/test/me")
                        .header("Authorization", "Bearer " + tokenForInactive))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security 7: Customer cannot access administrative analytics or AI actions")
    void testCustomerCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/ai/actions")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security 8: Staff cannot access administrative analytics or execute AI actions")
    void testStaffCannotAccessAdminActions() throws Exception {
        mockMvc.perform(get("/api/analytics/overview")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/ai/actions/1/execute")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security 9: Customer cannot access another customer's data")
    void testCrossCustomerAccessForbidden() throws Exception {
        // Customer 1 token attempting to access Customer 2 invoices
        mockMvc.perform(get("/api/customers/" + customer2.getId() + "/invoices")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security 10: OpenAPI / Swagger docs are accessible and document JWT scheme")
    void testOpenApiDocumentationAccessible() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", notNullValue()))
                .andExpect(jsonPath("$.components.securitySchemes['Bearer Authentication']", notNullValue()));
    }

    @Test
    @DisplayName("Security 11: Passwords and sensitive secrets are never returned in user DTOs")
    void testPasswordNeverReturned() throws Exception {
        mockMvc.perform(get("/api/test/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("Security 12: Rate limit filter triggers 429 when threshold exceeded")
    void testRateLimitExceeded() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin11@sec.com");
        loginRequest.setPassword("WrongPassword");

        // Execute rapid requests exceeding the 120 RPM threshold
        int status429Count = 0;
        for (int i = 0; i < 130; i++) {
            var result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();
            if (result.getResponse().getStatus() == 429) {
                status429Count++;
            }
        }

        org.assertj.core.api.Assertions.assertThat(status429Count).isGreaterThan(0);
    }
}

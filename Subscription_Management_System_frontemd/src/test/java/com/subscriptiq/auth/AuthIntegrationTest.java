package com.subscriptiq.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscriptiq.TestDataCleaner;
import com.subscriptiq.auth.dto.ForgotPasswordRequest;
import com.subscriptiq.auth.dto.LoginRequest;
import com.subscriptiq.auth.dto.RegisterRequest;
import com.subscriptiq.auth.dto.ResetPasswordRequest;
import com.subscriptiq.user.entity.PasswordResetToken;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.subscriptiq.SubscriptIqApplication.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthIntegrationTest {

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

    @BeforeEach
    void cleanUp() {
        dataCleaner.cleanAll();
    }

    @Test
    @DisplayName("POST /api/auth/register - Successfully register new customer")
    void testRegister_Success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .password("SecurePass@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Jane Doe")))
                .andExpect(jsonPath("$.data.email", is("jane.doe@example.com")))
                .andExpect(jsonPath("$.data.role", is("CUSTOMER")))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                .andExpect(jsonPath("$.data.password").doesNotExist());

        // Verify database persistence and password hashing
        User userInDb = userRepository.findByEmail("jane.doe@example.com").orElse(null);
        assertThat(userInDb).isNotNull();
        assertThat(userInDb.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(passwordEncoder.matches("SecurePass@123", userInDb.getPassword())).isTrue();
        assertThat(userInDb.getPassword()).startsWith("$2a$");
    }

    @Test
    @DisplayName("POST /api/auth/register - Reject duplicate email")
    void testRegister_DuplicateEmail() throws Exception {
        User existing = User.builder()
                .name("Existing")
                .email("duplicate@example.com")
                .password(passwordEncoder.encode("Pass@12345"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(existing);

        RegisterRequest request = RegisterRequest.builder()
                .name("New User")
                .email("duplicate@example.com")
                .password("Password@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("POST /api/auth/register - Validation rejects invalid email")
    void testRegister_InvalidEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Valid Name")
                .email("not-an-email")
                .password("Password@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.email", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/register - Validation rejects weak password (< 8 chars)")
    void testRegister_WeakPassword() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Valid Name")
                .email("valid@example.com")
                .password("short")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.password", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/login - Successfully login and receive JWT token")
    void testLogin_Success() throws Exception {
        User user = User.builder()
                .name("Login Test")
                .email("login@example.com")
                .password(passwordEncoder.encode("Secret@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .email("login@example.com")
                .password("Secret@123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.user.email", is("login@example.com")))
                .andExpect(jsonPath("$.data.user.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/login - Rejects invalid password")
    void testLogin_WrongPassword() throws Exception {
        User user = User.builder()
                .name("Login Test")
                .email("wrongpass@example.com")
                .password(passwordEncoder.encode("Secret@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .email("wrongpass@example.com")
                .password("IncorrectPassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", containsString("Invalid email or password")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Rejects inactive user")
    void testLogin_InactiveUser() throws Exception {
        User user = User.builder()
                .name("Inactive User")
                .email("inactive@example.com")
                .password(passwordEncoder.encode("Secret@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.INACTIVE)
                .build();
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .email("inactive@example.com")
                .password("Secret@123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", containsString("inactive")));
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password - Generic response prevents email enumeration")
    void testForgotPassword_GenericResponse() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("nonexistent@example.com")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("If the account exists")));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Successfully resets password with valid token")
    void testResetPassword_Success() throws Exception {
        User user = User.builder()
                .name("Reset User")
                .email("reset@example.com")
                .password(passwordEncoder.encode("OldPassword@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        PasswordResetToken token = PasswordResetToken.builder()
                .token("valid-test-token-12345")
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        resetTokenRepository.save(token);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("valid-test-token-12345")
                .newPassword("BrandNewPass@999")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Password has been successfully reset")));

        // Verify password changed and token is marked used
        User updatedUser = userRepository.findByEmail("reset@example.com").orElseThrow();
        assertThat(passwordEncoder.matches("BrandNewPass@999", updatedUser.getPassword())).isTrue();

        PasswordResetToken updatedToken = resetTokenRepository.findByToken("valid-test-token-12345").orElseThrow();
        assertThat(updatedToken.isUsed()).isTrue();
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Rejects expired reset token")
    void testResetPassword_ExpiredToken() throws Exception {
        User user = User.builder()
                .name("Reset User")
                .email("reset_expired@example.com")
                .password(passwordEncoder.encode("OldPassword@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        PasswordResetToken token = PasswordResetToken.builder()
                .token("expired-test-token-99999")
                .user(user)
                .expiryDate(LocalDateTime.now().minusMinutes(5))
                .used(false)
                .build();
        resetTokenRepository.save(token);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("expired-test-token-99999")
                .newPassword("BrandNewPass@999")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("expired")));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Rejects used reset token")
    void testResetPassword_UsedToken() throws Exception {
        User user = User.builder()
                .name("Reset User")
                .email("reset_used@example.com")
                .password(passwordEncoder.encode("OldPassword@123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        PasswordResetToken token = PasswordResetToken.builder()
                .token("used-test-token-88888")
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .used(true)
                .build();
        resetTokenRepository.save(token);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("used-test-token-88888")
                .newPassword("BrandNewPass@999")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already been used")));
    }
}


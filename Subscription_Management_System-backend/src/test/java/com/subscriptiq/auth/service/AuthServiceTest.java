package com.subscriptiq.auth.service;

import com.subscriptiq.auth.dto.ForgotPasswordRequest;
import com.subscriptiq.auth.dto.LoginRequest;
import com.subscriptiq.auth.dto.LoginResponse;
import com.subscriptiq.auth.dto.RegisterRequest;
import com.subscriptiq.auth.dto.ResetPasswordRequest;
import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.DuplicateResourceException;
import com.subscriptiq.common.exception.UnauthorizedException;
import com.subscriptiq.notification.service.EmailService;
import com.subscriptiq.security.JwtService;
import com.subscriptiq.user.dto.UserResponse;
import com.subscriptiq.user.entity.PasswordResetToken;
import com.subscriptiq.user.entity.User;
import com.subscriptiq.user.enums.UserRole;
import com.subscriptiq.user.enums.UserStatus;
import com.subscriptiq.user.repository.PasswordResetTokenRepository;
import com.subscriptiq.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository resetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "resetTokenExpirationMinutes", 15L);
    }

    @Test
    @DisplayName("Should successfully register customer with CUSTOMER role and hashed password")
    void testRegister_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice Wonder")
                .email("alice@example.com")
                .password("Password@123")
                .build();

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("$2a$10$hashedPassword");

        User savedUser = User.builder()
                .id(1L)
                .name("Alice Wonder")
                .email("alice@example.com")
                .password("$2a$10$hashedPassword")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User capturedUser = captor.getValue();
        assertThat(capturedUser.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(capturedUser.getPassword()).isEqualTo("$2a$10$hashedPassword");
    }

    @Test
    @DisplayName("Should reject registration if email already exists")
    void testRegister_DuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Bob")
                .email("bob@example.com")
                .password("Password@123")
                .build();

        when(userRepository.existsByEmail("bob@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("bob@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should login successfully and return JWT token")
    void testLogin_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("alice@example.com")
                .password("Password@123")
                .build();

        User user = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .password("$2a$10$hashedPassword")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(1L, "alice@example.com", "CUSTOMER")).thenReturn("mock-jwt-token");
        when(jwtService.getExpirationTimeMs()).thenReturn(86400000L);

        LoginResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400000L);
        assertThat(response.getUser().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("Should reject login when password is incorrect")
    void testLogin_WrongPassword() {
        LoginRequest request = LoginRequest.builder()
                .email("alice@example.com")
                .password("WrongPassword")
                .build();

        User user = User.builder()
                .id(1L)
                .email("alice@example.com")
                .password("$2a$10$hashedPassword")
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "$2a$10$hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    @DisplayName("Should reject login when account is inactive")
    void testLogin_InactiveAccount() {
        LoginRequest request = LoginRequest.builder()
                .email("alice@example.com")
                .password("Password@123")
                .build();

        User user = User.builder()
                .id(1L)
                .email("alice@example.com")
                .password("$2a$10$hashedPassword")
                .status(UserStatus.INACTIVE)
                .build();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    @DisplayName("Forgot password should generate reset token when user exists")
    void testForgotPassword_UserExists() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("alice@example.com")
                .build();

        User user = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        authService.forgotPassword(request);

        verify(resetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("alice@example.com"), eq("Alice"), anyString());
    }

    @Test
    @DisplayName("Forgot password should silently succeed without sending email if user does not exist")
    void testForgotPassword_UserNotFound() {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("unknown@example.com")
                .build();

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(request);

        verify(resetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Reset password should update password with hash and mark token used")
    void testResetPassword_Success() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("valid-token")
                .newPassword("NewSecurePass@123")
                .build();

        User user = User.builder()
                .id(1L)
                .email("alice@example.com")
                .password("old-hash")
                .status(UserStatus.ACTIVE)
                .build();

        PasswordResetToken token = PasswordResetToken.builder()
                .id(10L)
                .token("valid-token")
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(resetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewSecurePass@123")).thenReturn("$2a$10$newHash");

        authService.resetPassword(request);

        assertThat(token.isUsed()).isTrue();
        assertThat(user.getPassword()).isEqualTo("$2a$10$newHash");
        verify(userRepository).save(user);
        verify(resetTokenRepository).save(token);
    }

    @Test
    @DisplayName("Reset password should reject already used token")
    void testResetPassword_UsedToken() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("used-token")
                .newPassword("NewSecurePass@123")
                .build();

        PasswordResetToken token = PasswordResetToken.builder()
                .token("used-token")
                .used(true)
                .build();

        when(resetTokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    @DisplayName("Reset password should reject expired token")
    void testResetPassword_ExpiredToken() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("expired-token")
                .newPassword("NewSecurePass@123")
                .build();

        PasswordResetToken token = PasswordResetToken.builder()
                .token("expired-token")
                .expiryDate(LocalDateTime.now().minusMinutes(5))
                .used(false)
                .build();

        when(resetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }
}


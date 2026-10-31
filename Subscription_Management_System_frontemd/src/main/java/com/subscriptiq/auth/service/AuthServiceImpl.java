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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${application.security.reset-token.expiration-minutes:15}")
    private long resetTokenExpirationMinutes;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("User with email '" + normalizedEmail + "' already exists");
        }

        // Security rule: Public registration ALWAYS assigns CUSTOMER role.
        // Client input is never trusted for privilege elevation.
        User user = User.builder()
                .name(request.getName().trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new customer account with id [{}] and email [{}]", savedUser.getId(), savedUser.getEmail());

        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login attempt for inactive account: [{}]", normalizedEmail);
            throw new UnauthorizedException("Account is inactive. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password attempt for account: [{}]", normalizedEmail);
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTimeMs())
                .user(UserResponse.fromEntity(user))
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.getStatus() == UserStatus.ACTIVE) {
                String token = UUID.randomUUID().toString().replace("-", "");
                LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(resetTokenExpirationMinutes);

                PasswordResetToken resetToken = PasswordResetToken.builder()
                        .token(token)
                        .user(user)
                        .expiryDate(expiryDate)
                        .used(false)
                        .build();

                resetTokenRepository.save(resetToken);
                emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);
                log.info("Issued password reset token for user [{}]", normalizedEmail);
            } else {
                log.warn("Password reset requested for inactive user [{}] - skipping dispatch", normalizedEmail);
            }
        } else {
            // Constant-time mitigation / silent return to prevent email enumeration
            log.info("Password reset requested for non-existent email [{}] - silently ignored", normalizedEmail);
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new BadRequestException("Reset token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("User account is not eligible for password reset");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);

        log.info("Password successfully reset for user [{}]", user.getEmail());
    }
}


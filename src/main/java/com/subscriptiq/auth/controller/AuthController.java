package com.subscriptiq.auth.controller;

import com.subscriptiq.auth.dto.ForgotPasswordRequest;
import com.subscriptiq.auth.dto.LoginRequest;
import com.subscriptiq.auth.dto.LoginResponse;
import com.subscriptiq.auth.dto.RegisterRequest;
import com.subscriptiq.auth.dto.ResetPasswordRequest;
import com.subscriptiq.auth.service.AuthService;
import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, and password management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new customer", description = "Public registration endpoint. Always assigns CUSTOMER role.")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", user));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user credentials and issues a JWT token.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Generates a reset token and sends email instructions if the account exists.")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "If the account exists, password reset instructions have been sent."
        ));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets user password using a valid, unexpired reset token.")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password has been successfully reset."));
    }
}


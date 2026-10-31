package com.subscriptiq.common.controller;

import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.user.dto.CurrentUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Authorization Verification", description = "Test endpoints to verify role-based access control")
public class TestController {

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user info", description = "Accessible by any authenticated user.")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {
        CurrentUserResponse response = CurrentUserResponse.builder()
                .userId(principal.getId())
                .email(principal.getEmail())
                .role(principal.getRole())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Authenticated user retrieved", response));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ADMIN only test", description = "Accessible only by users with ADMIN role.")
    public ResponseEntity<ApiResponse<Map<String, String>>> testAdmin() {
        return ResponseEntity.ok(ApiResponse.success(
                "Access granted",
                Map.of("message", "Welcome Administrator. Full administrative access confirmed.")
        ));
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "ADMIN or STAFF test", description = "Accessible by users with ADMIN or STAFF role.")
    public ResponseEntity<ApiResponse<Map<String, String>>> testStaff() {
        return ResponseEntity.ok(ApiResponse.success(
                "Access granted",
                Map.of("message", "Welcome Staff member. Business operations access confirmed.")
        ));
    }

    @GetMapping("/customer")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'STAFF')")
    @Operation(summary = "CUSTOMER test", description = "Accessible by authenticated CUSTOMER, and ADMIN/STAFF.")
    public ResponseEntity<ApiResponse<Map<String, String>>> testCustomer() {
        return ResponseEntity.ok(ApiResponse.success(
                "Access granted",
                Map.of("message", "Welcome Customer. Customer portal access confirmed.")
        ));
    }
}


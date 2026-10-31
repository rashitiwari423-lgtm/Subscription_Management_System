package com.subscriptiq.customer.controller;

import com.subscriptiq.common.exception.UnauthorizedException;
import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.common.response.PagedResponse;
import com.subscriptiq.customer.dto.CreateCustomerRequest;
import com.subscriptiq.customer.dto.CustomerResponse;
import com.subscriptiq.customer.dto.UpdateCustomerRequest;
import com.subscriptiq.customer.dto.UpdateCustomerStatusRequest;
import com.subscriptiq.customer.enums.CustomerStatus;
import com.subscriptiq.customer.service.CustomerService;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.user.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Customer Management", description = "Customer profile management and directory")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Create customer", description = "Creates a new customer profile. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "List customers", description = "Paginated customer directory with search and status filter. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> getCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CustomerStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<CustomerResponse> response = customerService.getCustomers(search, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get customer by ID", description = "Retrieves customer details. CUSTOMER role can only access their own linked customer record.")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        CustomerResponse customer = customerService.getCustomerById(id);

        // Ownership enforcement for CUSTOMER role
        if (principal.getRole() == UserRole.CUSTOMER) {
            if (customer.getUserId() == null || !customer.getUserId().equals(principal.getId())) {
                throw new UnauthorizedException("Access denied: You do not have permission to view other customer records.");
            }
        }

        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Update customer", description = "Updates customer profile information. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Update customer status", description = "Activates or deactivates a customer. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomerStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerStatusRequest request) {
        CustomerResponse response = customerService.updateCustomerStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Customer status updated successfully", response));
    }
}


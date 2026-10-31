package com.subscriptiq.product.controller;

import com.subscriptiq.common.response.ApiResponse;
import com.subscriptiq.product.dto.CreateVariantRequest;
import com.subscriptiq.product.dto.ProductVariantResponse;
import com.subscriptiq.product.dto.UpdateVariantRequest;
import com.subscriptiq.product.dto.UpdateVariantStatusRequest;
import com.subscriptiq.product.service.ProductVariantService;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.user.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Product Variants", description = "Product variant (SKU) management and listing")
public class ProductVariantController {

    private final ProductVariantService variantService;

    @PostMapping("/api/products/{productId}/variants")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Create product variant", description = "Creates a new variant/SKU under specified product. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createVariant(
            @PathVariable Long productId,
            @Valid @RequestBody CreateVariantRequest request) {
        ProductVariantResponse response = variantService.createVariant(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product variant created successfully", response));
    }

    @GetMapping("/api/products/{productId}/variants")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List variants for product", description = "Retrieves all variants under a product. Customers only see ACTIVE variants.")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getVariantsByProductId(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean activeOnly = principal.getRole() == UserRole.CUSTOMER;
        List<ProductVariantResponse> variants = variantService.getVariantsByProductId(productId, activeOnly);
        return ResponseEntity.ok(ApiResponse.success(variants));
    }

    @GetMapping("/api/variants/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get variant by ID", description = "Retrieves variant details. Customers can only view ACTIVE variants.")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> getVariantById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean activeOnly = principal.getRole() == UserRole.CUSTOMER;
        ProductVariantResponse variant = variantService.getVariantById(id, activeOnly);
        return ResponseEntity.ok(ApiResponse.success(variant));
    }

    @PutMapping("/api/variants/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Update product variant", description = "Updates variant name, SKU, or description. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVariantRequest request) {
        ProductVariantResponse response = variantService.updateVariant(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product variant updated successfully", response));
    }

    @PatchMapping("/api/variants/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Update variant status", description = "Activates or deactivates a variant. Restricted to ADMIN and STAFF.")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariantStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVariantStatusRequest request) {
        ProductVariantResponse response = variantService.updateVariantStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Product variant status updated successfully", response));
    }
}


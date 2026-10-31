package com.subscriptiq.quotation.dto;

import com.subscriptiq.quotation.enums.QuotationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Quotation response payload")
public class QuotationResponse {

    @Schema(description = "Quotation ID", example = "1")
    private Long id;

    @Schema(description = "Unique quotation identifier", example = "QT-20261031-1001")
    private String quotationNumber;

    @Schema(description = "Customer ID", example = "5")
    private Long customerId;

    @Schema(description = "Customer company name", example = "Acme Corp")
    private String customerCompanyName;

    @Schema(description = "Customer email", example = "billing@acme.com")
    private String customerEmail;

    @Schema(description = "Quotation status", example = "DRAFT")
    private QuotationStatus status;

    @Schema(description = "Date of quotation issuance", example = "2026-10-31")
    private LocalDate quotationDate;

    @Schema(description = "Expiration date", example = "2026-11-30")
    private LocalDate validUntil;

    @Schema(description = "Subtotal amount before tax/discount", example = "1998.00")
    private BigDecimal subtotal;

    @Schema(description = "Total discount amount", example = "50.00")
    private BigDecimal discountAmount;

    @Schema(description = "Total tax amount", example = "18.00")
    private BigDecimal taxAmount;

    @Schema(description = "Net total amount", example = "1966.00")
    private BigDecimal totalAmount;

    @Schema(description = "Notes or commercial terms", example = "Standard terms apply")
    private String notes;

    @Schema(description = "Line items")
    private List<QuotationItemResponse> items;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}


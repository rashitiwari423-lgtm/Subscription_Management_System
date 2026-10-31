package com.subscriptiq.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private Long subscriptionId;

    @NotNull(message = "Invoice date is required")
    private LocalDate invoiceDate;

    private LocalDate billingPeriodStart;

    private LocalDate billingPeriodEnd;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private String notes;

    @NotEmpty(message = "Invoice must contain at least one item")
    @Valid
    private List<CreateInvoiceItemRequest> items;
}


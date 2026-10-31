package com.subscriptiq.tax.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.tax.dto.CreateTaxRequest;
import com.subscriptiq.tax.dto.TaxResponse;
import com.subscriptiq.tax.dto.UpdateTaxRequest;
import com.subscriptiq.tax.dto.UpdateTaxStatusRequest;
import com.subscriptiq.tax.entity.Tax;
import com.subscriptiq.tax.enums.TaxStatus;
import com.subscriptiq.tax.repository.TaxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxServiceTest {

    @Mock
    private TaxRepository taxRepository;

    private TaxService taxService;

    @BeforeEach
    void setUp() {
        taxService = new TaxServiceImpl(taxRepository);
    }

    @Test
    @DisplayName("Should successfully create a tax")
    void shouldCreateTax() {
        CreateTaxRequest request = CreateTaxRequest.builder()
                .name("VAT")
                .percentage(new BigDecimal("15.00"))
                .build();

        Tax saved = Tax.builder()
                .id(1L)
                .name("VAT")
                .percentage(new BigDecimal("15.00"))
                .status(TaxStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(taxRepository.existsByNameIgnoreCase("VAT")).thenReturn(false);
        when(taxRepository.save(any(Tax.class))).thenReturn(saved);

        TaxResponse response = taxService.createTax(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("VAT");
        assertThat(response.getPercentage()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(response.getStatus()).isEqualTo(TaxStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should throw BadRequestException when creating tax with duplicate name")
    void shouldThrowWhenDuplicateName() {
        CreateTaxRequest request = CreateTaxRequest.builder()
                .name("VAT")
                .percentage(new BigDecimal("15.00"))
                .build();

        when(taxRepository.existsByNameIgnoreCase("VAT")).thenReturn(true);

        assertThatThrownBy(() -> taxService.createTax(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should retrieve active taxes")
    void shouldGetActiveTaxes() {
        Tax t1 = Tax.builder().id(1L).name("VAT").percentage(new BigDecimal("10.00")).status(TaxStatus.ACTIVE).build();
        when(taxRepository.findByStatus(TaxStatus.ACTIVE)).thenReturn(List.of(t1));

        List<TaxResponse> active = taxService.getActiveTaxes();
        assertThat(active).hasSize(1);
        assertThat(active.get(0).getName()).isEqualTo("VAT");
    }

    @Test
    @DisplayName("Should update tax status to INACTIVE")
    void shouldUpdateTaxStatus() {
        Tax t1 = Tax.builder().id(1L).name("VAT").percentage(new BigDecimal("10.00")).status(TaxStatus.ACTIVE).build();
        when(taxRepository.findById(1L)).thenReturn(Optional.of(t1));
        when(taxRepository.save(any(Tax.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaxResponse response = taxService.updateTaxStatus(1L, UpdateTaxStatusRequest.builder().status(TaxStatus.INACTIVE).build());
        assertThat(response.getStatus()).isEqualTo(TaxStatus.INACTIVE);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when tax id does not exist")
    void shouldThrowWhenTaxNotFound() {
        when(taxRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxService.getTaxById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}


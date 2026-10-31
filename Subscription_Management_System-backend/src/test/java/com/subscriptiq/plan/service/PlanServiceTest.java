package com.subscriptiq.plan.service;

import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.plan.dto.CreatePlanRequest;
import com.subscriptiq.plan.dto.PlanResponse;
import com.subscriptiq.plan.dto.UpdatePlanRequest;
import com.subscriptiq.plan.entity.Plan;
import com.subscriptiq.plan.enums.BillingCycle;
import com.subscriptiq.plan.enums.PlanStatus;
import com.subscriptiq.plan.repository.PlanRepository;
import com.subscriptiq.product.entity.ProductVariant;
import com.subscriptiq.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private ProductVariantRepository variantRepository;

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanServiceImpl planService;

    @Test
    @DisplayName("Should create plan under valid variant with BigDecimal price")
    void testCreatePlan_Success() {
        ProductVariant variant = ProductVariant.builder()
                .id(1L)
                .name("Pro Edition")
                .sku("PRO-001")
                .build();

        CreatePlanRequest request = CreatePlanRequest.builder()
                .name("Monthly Pro")
                .description("Monthly subscription plan")
                .billingCycle(BillingCycle.MONTHLY)
                .price(new BigDecimal("999.00"))
                .currency("INR")
                .trialDays(14)
                .build();

        when(variantRepository.findById(1L)).thenReturn(Optional.of(variant));

        Plan saved = Plan.builder()
                .id(20L)
                .productVariant(variant)
                .name("Monthly Pro")
                .billingCycle(BillingCycle.MONTHLY)
                .price(new BigDecimal("999.00"))
                .currency("INR")
                .trialDays(14)
                .status(PlanStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(planRepository.save(any(Plan.class))).thenReturn(saved);

        PlanResponse response = planService.createPlan(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getProductVariantId()).isEqualTo(1L);
        assertThat(response.getPrice()).isEqualByComparingTo("999.00");
        assertThat(response.getCurrency()).isEqualTo("INR");
        assertThat(response.getTrialDays()).isEqualTo(14);
        assertThat(response.getBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
    }

    @Test
    @DisplayName("Should reject plan creation if variant does not exist")
    void testCreatePlan_NonExistentVariant() {
        CreatePlanRequest request = CreatePlanRequest.builder()
                .name("Yearly")
                .billingCycle(BillingCycle.YEARLY)
                .price(new BigDecimal("9999.00"))
                .build();

        when(variantRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planService.createPlan(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ProductVariant");
    }

    @Test
    @DisplayName("Should update plan successfully")
    void testUpdatePlan_Success() {
        Plan existing = Plan.builder()
                .id(10L)
                .name("Old Plan")
                .billingCycle(BillingCycle.MONTHLY)
                .price(new BigDecimal("499.00"))
                .currency("INR")
                .trialDays(7)
                .status(PlanStatus.ACTIVE)
                .build();

        UpdatePlanRequest request = UpdatePlanRequest.builder()
                .name("Updated Plan")
                .billingCycle(BillingCycle.YEARLY)
                .price(new BigDecimal("4999.00"))
                .currency("INR")
                .trialDays(30)
                .build();

        when(planRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(planRepository.save(any(Plan.class))).thenAnswer(i -> i.getArgument(0));

        PlanResponse response = planService.updatePlan(10L, request);

        assertThat(response.getName()).isEqualTo("Updated Plan");
        assertThat(response.getBillingCycle()).isEqualTo(BillingCycle.YEARLY);
        assertThat(response.getPrice()).isEqualByComparingTo("4999.00");
        assertThat(response.getTrialDays()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should deactivate plan status")
    void testUpdatePlanStatus_Deactivate() {
        Plan existing = Plan.builder()
                .id(10L)
                .name("Active Plan")
                .status(PlanStatus.ACTIVE)
                .build();

        when(planRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(planRepository.save(any(Plan.class))).thenAnswer(i -> i.getArgument(0));

        PlanResponse response = planService.updatePlanStatus(10L, PlanStatus.INACTIVE);

        assertThat(response.getStatus()).isEqualTo(PlanStatus.INACTIVE);
        verify(planRepository).save(existing);
    }
}


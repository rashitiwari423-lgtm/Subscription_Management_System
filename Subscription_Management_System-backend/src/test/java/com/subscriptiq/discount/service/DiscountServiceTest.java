package com.subscriptiq.discount.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.discount.dto.CreateDiscountRequest;
import com.subscriptiq.discount.dto.DiscountResponse;
import com.subscriptiq.discount.dto.UpdateDiscountRequest;
import com.subscriptiq.discount.dto.UpdateDiscountStatusRequest;
import com.subscriptiq.discount.entity.Discount;
import com.subscriptiq.discount.enums.DiscountStatus;
import com.subscriptiq.discount.enums.DiscountType;
import com.subscriptiq.discount.repository.DiscountRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountRepository discountRepository;

    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        discountService = new DiscountServiceImpl(discountRepository);
    }

    @Test
    @DisplayName("Should successfully create a percentage discount")
    void shouldCreatePercentageDiscount() {
        CreateDiscountRequest request = CreateDiscountRequest.builder()
                .name("PROMO10")
                .type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10.00"))
                .build();

        Discount saved = Discount.builder()
                .id(1L)
                .name("PROMO10")
                .type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10.00"))
                .status(DiscountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(discountRepository.existsByNameIgnoreCase("PROMO10")).thenReturn(false);
        when(discountRepository.save(any(Discount.class))).thenReturn(saved);

        DiscountResponse response = discountService.createDiscount(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("PROMO10");
        assertThat(response.getType()).isEqualTo(DiscountType.PERCENTAGE);
        assertThat(response.getValue()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("Should reject percentage discount exceeding 100%")
    void shouldRejectPercentageOver100() {
        CreateDiscountRequest request = CreateDiscountRequest.builder()
                .name("OVER100")
                .type(DiscountType.PERCENTAGE)
                .value(new BigDecimal("101.00"))
                .build();

        when(discountRepository.existsByNameIgnoreCase("OVER100")).thenReturn(false);

        assertThatThrownBy(() -> discountService.createDiscount(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot exceed 100%");
    }

    @Test
    @DisplayName("Should retrieve active discounts")
    void shouldGetActiveDiscounts() {
        Discount d1 = Discount.builder().id(1L).name("DISC").type(DiscountType.FIXED).value(new BigDecimal("20.00")).status(DiscountStatus.ACTIVE).build();
        when(discountRepository.findByStatus(DiscountStatus.ACTIVE)).thenReturn(List.of(d1));

        List<DiscountResponse> active = discountService.getActiveDiscounts();
        assertThat(active).hasSize(1);
        assertThat(active.get(0).getName()).isEqualTo("DISC");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when discount id does not exist")
    void shouldThrowWhenDiscountNotFound() {
        when(discountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> discountService.getDiscountById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}


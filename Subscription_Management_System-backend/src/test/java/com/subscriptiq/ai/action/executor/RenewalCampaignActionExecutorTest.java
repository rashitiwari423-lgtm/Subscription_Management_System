package com.subscriptiq.ai.action.executor;

import com.subscriptiq.ai.action.dto.ActionExecutionResult;
import com.subscriptiq.ai.action.dto.ActionPreviewDto;
import com.subscriptiq.ai.action.entity.AiActionRequest;
import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.customer.entity.Customer;
import com.subscriptiq.security.UserPrincipal;
import com.subscriptiq.subscription.entity.Subscription;
import com.subscriptiq.subscription.enums.SubscriptionStatus;
import com.subscriptiq.subscription.repository.SubscriptionRepository;
import com.subscriptiq.user.entity.User;
import com.subscriptiq.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RenewalCampaignActionExecutorTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private RenewalCampaignActionExecutor executor;

    private UserPrincipal adminPrincipal;
    private Customer customer;
    private Subscription expiringSub;

    @BeforeEach
    void setUp() {
        User adminUser = User.builder()
                .id(1L)
                .email("admin@test.com")
                .role(UserRole.ADMIN)
                .build();
        adminPrincipal = UserPrincipal.create(adminUser);

        customer = Customer.builder()
                .id(100L)
                .companyName("Acme Corp")
                .email("contact@acme.com")
                .build();

        expiringSub = Subscription.builder()
                .id(50L)
                .subscriptionNumber("SUB-2026-001")
                .customer(customer)
                .status(SubscriptionStatus.ACTIVE)
                .totalAmount(new BigDecimal("1200.00"))
                .endDate(LocalDate.now().plusDays(10))
                .build();
    }

    @Test
    @DisplayName("getActionType - Returns SEND_RENEWAL_CAMPAIGN")
    void testGetActionType() {
        assertThat(executor.getActionType()).isEqualTo(AiActionType.SEND_RENEWAL_CAMPAIGN);
    }

    @Test
    @DisplayName("preview - Correctly calculates expiring subscriptions and revenue")
    void testPreview() {
        when(subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(expiringSub));

        AiInsight insight = AiInsight.builder().id(1L).build();
        ActionPreviewDto preview = executor.preview(insight);

        assertThat(preview.getAffectedCustomers()).isEqualTo(1);
        assertThat(preview.getAffectedSubscriptions()).isEqualTo(1);
        assertThat(preview.getPotentialRevenue()).isEqualByComparingTo("1200.00");
        assertThat(preview.getAffectedResources()).hasSize(1);
        assertThat(preview.getWarnings()).isNotEmpty();
    }

    @Test
    @DisplayName("execute - Prepares campaign without fake email dispatch")
    void testExecute() {
        when(subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(expiringSub));

        AiActionRequest action = AiActionRequest.builder()
                .id(10L)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .build();

        ActionExecutionResult result = executor.execute(action, adminPrincipal);

        assertThat(result.getStatus()).isEqualTo(AiActionExecutionStatus.SUCCESS);
        assertThat(result.getAffectedCount()).isEqualTo(1);
        assertThat(result.getSummary()).contains("Renewal campaign prepared for 1 customers");
    }
}

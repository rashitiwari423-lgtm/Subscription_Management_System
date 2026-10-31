package com.subscriptiq.ai.action.validator;

import com.subscriptiq.ai.action.dto.ActionPreviewRequest;
import com.subscriptiq.ai.action.entity.AiActionRequest;
import com.subscriptiq.ai.action.enums.AiActionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import com.subscriptiq.ai.entity.AiInsight;
import com.subscriptiq.ai.enums.AiInsightStatus;
import com.subscriptiq.ai.enums.AiRecommendedAction;
import com.subscriptiq.ai.repository.AiInsightRepository;
import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiActionValidatorTest {

    @Mock
    private AiInsightRepository aiInsightRepository;

    @InjectMocks
    private AiActionValidator validator;

    private AiInsight validInsight;

    @BeforeEach
    void setUp() {
        validInsight = AiInsight.builder()
                .id(1L)
                .type("EXPIRING_SUBSCRIPTIONS")
                .title("Expiring Subscriptions Detected")
                .status(AiInsightStatus.NEW)
                .recommendedAction(AiRecommendedAction.SEND_RENEWAL_CAMPAIGN)
                .build();
    }

    @Test
    @DisplayName("validatePreviewRequest - Valid request succeeds and returns insight")
    void testValidatePreviewRequest_Valid() {
        when(aiInsightRepository.findById(1L)).thenReturn(Optional.of(validInsight));

        ActionPreviewRequest request = ActionPreviewRequest.builder()
                .insightId(1L)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .build();

        AiInsight result = validator.validatePreviewRequest(request);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("validatePreviewRequest - Non-existent insight throws ResourceNotFoundException")
    void testValidatePreviewRequest_NotFound() {
        when(aiInsightRepository.findById(999L)).thenReturn(Optional.empty());

        ActionPreviewRequest request = ActionPreviewRequest.builder()
                .insightId(999L)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .build();

        assertThatThrownBy(() -> validator.validatePreviewRequest(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("AI Insight not found");
    }

    @Test
    @DisplayName("validatePreviewRequest - Incompatible action type throws BadRequestException")
    void testValidatePreviewRequest_IncompatibleAction() {
        when(aiInsightRepository.findById(1L)).thenReturn(Optional.of(validInsight));

        ActionPreviewRequest request = ActionPreviewRequest.builder()
                .insightId(1L)
                .actionType(AiActionType.FOLLOW_UP_OVERDUE_CUSTOMERS)
                .build();

        assertThatThrownBy(() -> validator.validatePreviewRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not match insight recommendation");
    }

    @Test
    @DisplayName("validatePreviewRequest - Resolved insight throws BadRequestException")
    void testValidatePreviewRequest_ResolvedInsight() {
        validInsight.setStatus(AiInsightStatus.RESOLVED);
        when(aiInsightRepository.findById(1L)).thenReturn(Optional.of(validInsight));

        ActionPreviewRequest request = ActionPreviewRequest.builder()
                .insightId(1L)
                .actionType(AiActionType.SEND_RENEWAL_CAMPAIGN)
                .build();

        assertThatThrownBy(() -> validator.validatePreviewRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("RESOLVED and cannot be acted upon");
    }

    @Test
    @DisplayName("validateApprovalRequest - Non-pending action throws BadRequestException")
    void testValidateApprovalRequest_NotPending() {
        AiActionRequest action = AiActionRequest.builder()
                .id(10L)
                .status(AiActionStatus.APPROVED)
                .build();

        assertThatThrownBy(() -> validator.validateApprovalRequest(action))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("is not pending approval");
    }

    @Test
    @DisplayName("validateExecutionRequest - Unapproved action throws BadRequestException")
    void testValidateExecutionRequest_Unapproved() {
        AiActionRequest action = AiActionRequest.builder()
                .id(10L)
                .status(AiActionStatus.PENDING_APPROVAL)
                .build();

        assertThatThrownBy(() -> validator.validateExecutionRequest(action))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must be approved by an administrator before execution");
    }

    @Test
    @DisplayName("validateExecutionRequest - Rejected action throws BadRequestException")
    void testValidateExecutionRequest_Rejected() {
        AiActionRequest action = AiActionRequest.builder()
                .id(10L)
                .status(AiActionStatus.REJECTED)
                .build();

        assertThatThrownBy(() -> validator.validateExecutionRequest(action))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("because it has been REJECTED");
    }
}


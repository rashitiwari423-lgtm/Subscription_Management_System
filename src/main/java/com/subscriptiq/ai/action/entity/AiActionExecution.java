package com.subscriptiq.ai.action.entity;

import com.subscriptiq.ai.action.enums.AiActionExecutionStatus;
import com.subscriptiq.ai.action.enums.AiActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ai_action_executions",
        indexes = {
                @Index(name = "idx_ai_action_exec_action_id", columnList = "action_id"),
                @Index(name = "idx_ai_action_exec_type", columnList = "action_type"),
                @Index(name = "idx_ai_action_exec_status", columnList = "status"),
                @Index(name = "idx_ai_action_exec_executed_at", columnList = "executed_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiActionExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    private AiActionRequest action;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private AiActionType actionType;

    @Column(name = "executed_by", nullable = false, length = 150)
    private String executedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AiActionExecutionStatus status;

    @Column(name = "affected_count", nullable = false)
    @Builder.Default
    private Integer affectedCount = 0;

    @Column(name = "result_summary", columnDefinition = "TEXT")
    private String resultSummary;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.executedAt == null) {
            this.executedAt = LocalDateTime.now();
        }
        if (this.affectedCount == null) {
            this.affectedCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}


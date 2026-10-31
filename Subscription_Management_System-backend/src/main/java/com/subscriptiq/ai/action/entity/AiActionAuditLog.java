package com.subscriptiq.ai.action.entity;

import com.subscriptiq.ai.action.enums.AiActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ai_action_audit_logs",
        indexes = {
                @Index(name = "idx_ai_audit_action_id", columnList = "action_id"),
                @Index(name = "idx_ai_audit_insight_id", columnList = "insight_id"),
                @Index(name = "idx_ai_audit_action_type", columnList = "action_type"),
                @Index(name = "idx_ai_audit_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiActionAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String actor;

    @Column(nullable = false, length = 50)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private AiActionType actionType;

    @Column(name = "action_id")
    private Long actionId;

    @Column(name = "insight_id")
    private Long insightId;

    @Column(nullable = false, length = 50)
    private String result;

    @Column(name = "affected_count", nullable = false)
    @Builder.Default
    private Integer affectedCount = 0;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.affectedCount == null) {
            this.affectedCount = 0;
        }
    }
}


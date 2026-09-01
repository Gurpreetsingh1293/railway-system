package com.railways.blockplanning.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A formal block request submitted via BDMS (Block Demand Management System).
 * Links to a specific defect and requests a maintenance window on a corridor.
 */
@Entity
@Table(name = "block_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defect_id", nullable = false)
    private Defect defect;

    @Column(name = "requesting_department", nullable = false, length = 50)
    private String requestingDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corridor_id", nullable = false)
    private Corridor corridor;

    @Column(name = "requested_on", nullable = false)
    private LocalDate requestedOn;

    @Column(name = "requested_window_date", nullable = false)
    private LocalDate requestedWindowDate;

    /** Hour of day (0-23) when the block should start */
    @Column(name = "requested_start_hour", nullable = false)
    private Integer requestedStartHour;

    @Column(name = "requested_duration_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal requestedDurationHours;

    /** Priority score — inherited from the linked defect's score */
    @Column(name = "priority_score", precision = 8, scale = 4)
    private BigDecimal priorityScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 10)
    private ApprovalStatus approvalStatus;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.approvalStatus == null) this.approvalStatus = ApprovalStatus.Pending;
        if (this.priorityScore == null) this.priorityScore = BigDecimal.ZERO;
    }
}

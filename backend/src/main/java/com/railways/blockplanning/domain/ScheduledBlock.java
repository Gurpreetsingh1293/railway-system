package com.railways.blockplanning.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * ScheduledBlock — the OUTPUT of the Timefold Solver.
 *
 * Represents a maintenance window that has been assigned by the optimizer.
 * A single scheduled block can contain multiple block requests (bundled = true)
 * from different departments on the same corridor in the same time window.
 *
 * This bundling is the core optimization insight:
 * instead of Engineering, S&T, and Traction each taking separate windows,
 * they share one window — reducing total downtime.
 */
@Entity
@Table(name = "scheduled_block")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corridor_id", nullable = false)
    private Corridor corridor;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_horizon", nullable = false, length = 10)
    private PlanHorizon planHorizon;

    /** The date this plan was generated */
    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    /** The actual date the maintenance block is scheduled */
    @Column(name = "block_date", nullable = false)
    private LocalDate blockDate;

    /** Start hour (0-23) of the maintenance window */
    @Column(name = "block_start_hour", nullable = false)
    private Integer blockStartHour;

    @Column(name = "block_duration_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal blockDurationHours;

    /** True if this block contains tasks from more than one department */
    @Column(name = "is_bundled", nullable = false)
    private Boolean isBundled;

    /** Comma-separated list of departments in this block */
    @Column(name = "departments_involved")
    private String departmentsInvolved;

    /** Sum of priority scores for all contained block requests */
    @Column(name = "total_priority_score", precision = 10, scale = 4)
    private BigDecimal totalPriorityScore;

    /** Which scoring mode produced the priorities (RULE_BASED or ML) */
    @Column(name = "scoring_mode", length = 20)
    private String scoringMode;

    @ManyToMany
    @JoinTable(
        name = "scheduled_block_requests",
        joinColumns = @JoinColumn(name = "scheduled_block_id"),
        inverseJoinColumns = @JoinColumn(name = "block_request_id")
    )
    @Builder.Default
    private Set<BlockRequest> blockRequests = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.isBundled == null) this.isBundled = false;
        if (this.totalPriorityScore == null) this.totalPriorityScore = BigDecimal.ZERO;
        if (this.scoringMode == null) this.scoringMode = "RULE_BASED";
    }
}

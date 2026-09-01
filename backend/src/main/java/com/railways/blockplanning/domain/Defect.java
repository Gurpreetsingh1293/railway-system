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
 * A maintenance defect on a corridor, ingested from one of the three source systems:
 * - TMS  (Track Management System)       — Engineering dept
 * - SMMS (Signal & Maintenance Mgmt Sys) — S&T dept
 * - TDMS (Traction Distribution Mgmt Sys)— Traction Distribution dept
 *
 * NOTE: All data in dev/demo is SYNTHETIC — no real system access exists.
 */
@Entity
@Table(name = "defect")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Defect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_system", nullable = false, length = 10)
    private SourceSystem sourceSystem;

    @Column(name = "department", nullable = false, length = 50)
    private String department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corridor_id", nullable = false)
    private Corridor corridor;

    @Column(name = "asset_type", nullable = false)
    private String assetType;

    @Column(name = "km_marker", precision = 8, scale = 2)
    private BigDecimal kmMarker;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private Severity severity;

    @Column(name = "date_raised", nullable = false)
    private LocalDate dateRaised;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private DefectStatus status;

    @Column(name = "estimated_repair_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal estimatedRepairHours;

    /**
     * Computed priority score — see PriorityScoreService.
     * Higher = more urgent. Recomputed whenever scoring runs.
     */
    @Column(name = "priority_score", precision = 8, scale = 4)
    private BigDecimal priorityScore;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.status == null) this.status = DefectStatus.Open;
        if (this.priorityScore == null) this.priorityScore = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

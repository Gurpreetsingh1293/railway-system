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
 * COA (Corridor Availability) — a time window on a corridor when no trains
 * are operating and maintenance work is permitted.
 * Derived from the train timetable (synthetic in this prototype).
 */
@Entity
@Table(
    name = "coa_availability",
    uniqueConstraints = @UniqueConstraint(columnNames = {"corridor_id", "available_date", "window_start_hour"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoaAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corridor_id", nullable = false)
    private Corridor corridor;

    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    /** Hour of day (0-23) when the maintenance window starts */
    @Column(name = "window_start_hour", nullable = false)
    private Integer windowStartHour;

    /** Maximum hours available in this window */
    @Column(name = "max_duration_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal maxDurationHours;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

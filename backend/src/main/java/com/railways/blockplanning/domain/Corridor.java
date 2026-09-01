package com.railways.blockplanning.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A railway corridor — a track section between two stations.
 * Example: Mumbai CST - Pune (192 km, Central Railway, Mumbai Division)
 */
@Entity
@Table(name = "corridor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Corridor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "corridor_name", nullable = false)
    private String corridorName;

    @Column(name = "zone", nullable = false, length = 10)
    private String zone;

    @Column(name = "division", nullable = false)
    private String division;

    @Column(name = "route_km", nullable = false, precision = 8, scale = 2)
    private BigDecimal routeKm;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

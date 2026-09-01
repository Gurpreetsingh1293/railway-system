package com.railways.blockplanning.api.dto;

import lombok.Data;

/**
 * Scoring weights DTO — used by the Config screen in the frontend.
 * Allows live tuning of weights without application restart.
 */
@Data
public class ScoringWeightsDto {
    private String mode; // RULE_BASED | ML
    private Double severityCritical;
    private Double severityMajor;
    private Double severityMinor;
    private Double overdueFactor;
    private Integer maxOverdueDays;
    private Double safetyHigh;
    private Double safetyMedium;
    private Double safetyLow;
}

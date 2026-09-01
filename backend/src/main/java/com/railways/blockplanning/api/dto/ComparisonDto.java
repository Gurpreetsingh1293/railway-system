package com.railways.blockplanning.api.dto;

import lombok.Data;

/**
 * Before/after comparison DTO — used by the comparison dashboard view.
 * Shows the optimization benefit: naive (uncoordinated) vs. optimized (bundled).
 */
@Data
public class ComparisonDto {
    private String horizon;
    private double naiveDowntimeHours;
    private double optimizedDowntimeHours;
    private double savedHours;
    private double savingsPercent;
    private int totalScheduledBlocks;
    private int bundledBlocks;
    private int scheduledRequests;
}

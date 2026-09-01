package com.railways.blockplanning.api.dto;

import com.railways.blockplanning.domain.PlanHorizon;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for a scheduled block — used by the Gantt chart in the frontend.
 * Contains everything needed to render a Gantt row.
 */
@Data
public class ScheduledBlockDto {
    private Long id;
    private Long corridorId;
    private String corridorName;
    private PlanHorizon planHorizon;
    private LocalDate planDate;
    private LocalDate blockDate;
    private Integer blockStartHour;
    private BigDecimal blockDurationHours;
    private Boolean isBundled;
    private String departmentsInvolved;
    private BigDecimal totalPriorityScore;
    private String scoringMode;
    private List<Long> blockRequestIds;
}

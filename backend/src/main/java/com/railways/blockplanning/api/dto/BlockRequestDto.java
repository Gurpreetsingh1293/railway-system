package com.railways.blockplanning.api.dto;

import com.railways.blockplanning.domain.ApprovalStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BlockRequestDto {
    private Long id;
    private Long defectId;
    private String requestingDepartment;
    private Long corridorId;
    private String corridorName;
    private LocalDate requestedOn;
    private LocalDate requestedWindowDate;
    private Integer requestedStartHour;
    private BigDecimal requestedDurationHours;
    private BigDecimal priorityScore;
    private ApprovalStatus approvalStatus;
}

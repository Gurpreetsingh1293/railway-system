package com.railways.blockplanning.api.dto;

import com.railways.blockplanning.domain.Severity;
import com.railways.blockplanning.domain.SourceSystem;
import com.railways.blockplanning.domain.DefectStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for defect data — returned by the API, never exposes JPA internals.
 * The frontend uses this to render the defect backlog table.
 */
@Data
public class DefectDto {
    private Long id;
    private SourceSystem sourceSystem;
    private String department;
    private Long corridorId;
    private String corridorName;
    private String assetType;
    private BigDecimal kmMarker;
    private Severity severity;
    private LocalDate dateRaised;
    private LocalDate dueDate;
    private DefectStatus status;
    private BigDecimal estimatedRepairHours;
    private BigDecimal priorityScore;
}

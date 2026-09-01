package com.railways.blockplanning.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CoaAvailabilityDto {
    private Long id;
    private Long corridorId;
    private String corridorName;
    private LocalDate availableDate;
    private Integer windowStartHour;
    private BigDecimal maxDurationHours;
    private String reason;
}

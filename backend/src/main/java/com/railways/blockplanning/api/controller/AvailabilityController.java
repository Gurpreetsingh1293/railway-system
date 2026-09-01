package com.railways.blockplanning.api.controller;

import com.railways.blockplanning.api.dto.ApiResponse;
import com.railways.blockplanning.api.dto.CoaAvailabilityDto;
import com.railways.blockplanning.domain.CoaAvailability;
import com.railways.blockplanning.repository.CoaAvailabilityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
@Tag(name = "COA Availability", description = "Corridor availability windows (from train timetable)")
@CrossOrigin(origins = "*")
public class AvailabilityController {

    private final CoaAvailabilityRepository coaAvailabilityRepository;

    @GetMapping
    @Operation(summary = "List all availability windows, with optional date range filter")
    public ResponseEntity<ApiResponse<List<CoaAvailabilityDto>>> listAll(
            @RequestParam(required = false) Long corridorId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        List<CoaAvailability> windows;
        if (corridorId != null && from != null && to != null) {
            windows = coaAvailabilityRepository.findByCorridorIdAndAvailableDateBetween(
                corridorId, LocalDate.parse(from), LocalDate.parse(to));
        } else if (from != null && to != null) {
            windows = coaAvailabilityRepository.findByAvailableDateBetween(
                LocalDate.parse(from), LocalDate.parse(to));
        } else if (corridorId != null) {
            windows = coaAvailabilityRepository
                .findByCorridorIdOrderByAvailableDateAscWindowStartHourAsc(corridorId);
        } else {
            windows = coaAvailabilityRepository.findAll();
        }

        List<CoaAvailabilityDto> dtos = windows.stream().map(this::toDto).toList();
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    private CoaAvailabilityDto toDto(CoaAvailability a) {
        CoaAvailabilityDto dto = new CoaAvailabilityDto();
        dto.setId(a.getId());
        dto.setCorridorId(a.getCorridor() != null ? a.getCorridor().getId() : null);
        dto.setCorridorName(a.getCorridor() != null ? a.getCorridor().getCorridorName() : null);
        dto.setAvailableDate(a.getAvailableDate());
        dto.setWindowStartHour(a.getWindowStartHour());
        dto.setMaxDurationHours(a.getMaxDurationHours());
        dto.setReason(a.getReason());
        return dto;
    }
}

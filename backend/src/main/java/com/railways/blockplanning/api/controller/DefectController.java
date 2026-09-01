package com.railways.blockplanning.api.controller;

import com.railways.blockplanning.api.dto.ApiResponse;
import com.railways.blockplanning.api.dto.DefectDto;
import com.railways.blockplanning.domain.*;
import com.railways.blockplanning.repository.DefectRepository;
import com.railways.blockplanning.scoring.PriorityScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/defects")
@RequiredArgsConstructor
@Tag(name = "Defects", description = "Maintenance defect management (TMS/SMMS/TDMS ingestion)")
@CrossOrigin(origins = "*")
public class DefectController {

    private final DefectRepository defectRepository;
    private final PriorityScoreService priorityScoreService;

    @GetMapping
    @Operation(summary = "List all defects, sorted by priority score descending")
    public ResponseEntity<ApiResponse<List<DefectDto>>> listAll(
            @RequestParam(required = false) String sourceSystem,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long corridorId) {

        List<Defect> defects = defectRepository.findAll();

        // Apply optional filters
        if (sourceSystem != null)
            defects = defects.stream().filter(d -> d.getSourceSystem().name().equalsIgnoreCase(sourceSystem)).toList();
        if (severity != null)
            defects = defects.stream().filter(d -> d.getSeverity().name().equalsIgnoreCase(severity)).toList();
        if (status != null)
            defects = defects.stream().filter(d -> d.getStatus().name().equalsIgnoreCase(status)).toList();
        if (corridorId != null)
            defects = defects.stream().filter(d -> d.getCorridor().getId().equals(corridorId)).toList();

        List<DefectDto> dtos = defects.stream()
            .sorted((a, b) -> {
                if (a.getPriorityScore() == null && b.getPriorityScore() == null) return 0;
                if (a.getPriorityScore() == null) return 1;
                if (b.getPriorityScore() == null) return -1;
                return b.getPriorityScore().compareTo(a.getPriorityScore());
            })
            .map(this::toDto)
            .toList();

        return ResponseEntity.ok(ApiResponse.ok(dtos, priorityScoreService.currentMode()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get defect by ID")
    public ResponseEntity<ApiResponse<DefectDto>> getById(@PathVariable Long id) {
        return defectRepository.findById(id)
            .map(d -> ResponseEntity.ok(ApiResponse.ok(toDto(d), priorityScoreService.currentMode())))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/score")
    @Operation(summary = "Recompute priority scores for all active defects")
    public ResponseEntity<ApiResponse<String>> rescoreAll() {
        int count = priorityScoreService.scoreAllActiveDefects();
        return ResponseEntity.ok(ApiResponse.ok(
            "Scored " + count + " defects",
            priorityScoreService.currentMode()
        ));
    }

    private DefectDto toDto(Defect d) {
        DefectDto dto = new DefectDto();
        dto.setId(d.getId());
        dto.setSourceSystem(d.getSourceSystem());
        dto.setDepartment(d.getDepartment());
        dto.setCorridorId(d.getCorridor().getId());
        dto.setCorridorName(d.getCorridor().getCorridorName());
        dto.setAssetType(d.getAssetType());
        dto.setKmMarker(d.getKmMarker());
        dto.setSeverity(d.getSeverity());
        dto.setDateRaised(d.getDateRaised());
        dto.setDueDate(d.getDueDate());
        dto.setStatus(d.getStatus());
        dto.setEstimatedRepairHours(d.getEstimatedRepairHours());
        dto.setPriorityScore(d.getPriorityScore());
        return dto;
    }
}

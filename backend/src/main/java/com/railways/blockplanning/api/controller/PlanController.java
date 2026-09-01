package com.railways.blockplanning.api.controller;

import com.railways.blockplanning.api.dto.ApiResponse;
import com.railways.blockplanning.api.dto.ComparisonDto;
import com.railways.blockplanning.api.dto.ScheduledBlockDto;
import com.railways.blockplanning.domain.PlanHorizon;
import com.railways.blockplanning.domain.ScheduledBlock;
import com.railways.blockplanning.repository.ScheduledBlockRepository;
import com.railways.blockplanning.scheduling.BlockPlanSchedulerService;
import com.railways.blockplanning.scoring.PriorityScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Block Plans", description = "Schedule generation and Gantt data")
@CrossOrigin(origins = "*")
public class PlanController {

    private final BlockPlanSchedulerService schedulerService;
    private final ScheduledBlockRepository scheduledBlockRepository;
    private final PriorityScoreService priorityScoreService;

    @PostMapping("/generate")
    @Operation(summary = "Generate a new block plan for the given horizon (WEEKLY or MONTHLY)")
    public ResponseEntity<ApiResponse<List<ScheduledBlockDto>>> generate(
            @RequestParam(defaultValue = "WEEKLY") PlanHorizon horizon) {

        // First, rescore all defects to get fresh priority scores
        priorityScoreService.scoreAllActiveDefects();

        List<ScheduledBlock> blocks = schedulerService.generatePlan(horizon);
        List<ScheduledBlockDto> dtos = blocks.stream().map(this::toDto).toList();

        return ResponseEntity.ok(ApiResponse.ok(dtos, priorityScoreService.currentMode()));
    }

    @GetMapping
    @Operation(summary = "Get the current block plan (for Gantt rendering)")
    public ResponseEntity<ApiResponse<List<ScheduledBlockDto>>> getPlan(
            @RequestParam(defaultValue = "WEEKLY") PlanHorizon horizon,
            @RequestParam(required = false) Long corridorId) {

        List<ScheduledBlock> blocks;
        if (corridorId != null) {
            blocks = scheduledBlockRepository.findByCorridorIdAndPlanHorizon(corridorId, horizon);
        } else {
            blocks = scheduledBlockRepository.findByPlanHorizonOrderByBlockDateAscBlockStartHourAsc(horizon);
        }

        List<ScheduledBlockDto> dtos = blocks.stream().map(this::toDto).toList();
        return ResponseEntity.ok(ApiResponse.ok(dtos, priorityScoreService.currentMode()));
    }

    @GetMapping("/comparison")
    @Operation(summary = "Get before/after downtime comparison for the given horizon")
    public ResponseEntity<ApiResponse<ComparisonDto>> getComparison(
            @RequestParam(defaultValue = "WEEKLY") PlanHorizon horizon) {

        double naive = schedulerService.computeNaiveDowntimeHours(horizon);
        double optimized = schedulerService.computeOptimizedDowntimeHours(horizon);
        double saved = Math.max(0, naive - optimized);
        double savingsPercent = naive > 0 ? (saved / naive) * 100.0 : 0.0;

        List<ScheduledBlock> blocks = scheduledBlockRepository
            .findByPlanHorizonOrderByBlockDateAscBlockStartHourAsc(horizon);

        ComparisonDto dto = new ComparisonDto();
        dto.setHorizon(horizon.name());
        dto.setNaiveDowntimeHours(naive);
        dto.setOptimizedDowntimeHours(optimized);
        dto.setSavedHours(saved);
        dto.setSavingsPercent(Math.round(savingsPercent * 10.0) / 10.0);
        dto.setTotalScheduledBlocks(blocks.size());
        dto.setBundledBlocks((int) blocks.stream().filter(ScheduledBlock::getIsBundled).count());
        dto.setScheduledRequests(blocks.stream()
            .mapToInt(b -> b.getBlockRequests().size()).sum());

        return ResponseEntity.ok(ApiResponse.ok(dto, priorityScoreService.currentMode()));
    }

    private ScheduledBlockDto toDto(ScheduledBlock sb) {
        ScheduledBlockDto dto = new ScheduledBlockDto();
        dto.setId(sb.getId());
        dto.setCorridorId(sb.getCorridor().getId());
        dto.setCorridorName(sb.getCorridor().getCorridorName());
        dto.setPlanHorizon(sb.getPlanHorizon());
        dto.setPlanDate(sb.getPlanDate());
        dto.setBlockDate(sb.getBlockDate());
        dto.setBlockStartHour(sb.getBlockStartHour());
        dto.setBlockDurationHours(sb.getBlockDurationHours());
        dto.setIsBundled(sb.getIsBundled());
        dto.setDepartmentsInvolved(sb.getDepartmentsInvolved());
        dto.setTotalPriorityScore(sb.getTotalPriorityScore());
        dto.setScoringMode(sb.getScoringMode());
        dto.setBlockRequestIds(sb.getBlockRequests().stream()
            .map(r -> r.getId()).toList());
        return dto;
    }
}

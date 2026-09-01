package com.railways.blockplanning.api.controller;

import com.railways.blockplanning.api.dto.ApiResponse;
import com.railways.blockplanning.api.dto.ScoringWeightsDto;
import com.railways.blockplanning.scoring.PriorityScoreService;
import com.railways.blockplanning.scoring.ScoringProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Config controller — allows live tuning of scoring weights and mode.
 * This powers the Config page in the frontend where demo judges can
 * see weights and switch between RULE_BASED and ML scoring modes.
 */
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@Tag(name = "Configuration", description = "Live scoring configuration and weight tuning")
@CrossOrigin(origins = "*")
public class ConfigController {

    private final PriorityScoreService priorityScoreService;
    private final ScoringProperties scoringProperties;

    @GetMapping("/scoring")
    @Operation(summary = "Get current scoring configuration")
    public ResponseEntity<ApiResponse<ScoringWeightsDto>> getScoringConfig() {
        return ResponseEntity.ok(ApiResponse.ok(toDto(), priorityScoreService.currentMode()));
    }

    @PutMapping("/scoring")
    @Operation(summary = "Update scoring weights and/or mode at runtime (no restart needed)")
    public ResponseEntity<ApiResponse<ScoringWeightsDto>> updateScoringConfig(
            @RequestBody ScoringWeightsDto dto) {

        if (dto.getMode() != null) {
            priorityScoreService.setMode(dto.getMode());
        }

        ScoringProperties.Weights weights = scoringProperties.getWeights();

        if (dto.getSeverityCritical() != null)
            weights.getSeverity().setCritical(dto.getSeverityCritical());
        if (dto.getSeverityMajor() != null)
            weights.getSeverity().setMajor(dto.getSeverityMajor());
        if (dto.getSeverityMinor() != null)
            weights.getSeverity().setMinor(dto.getSeverityMinor());
        if (dto.getOverdueFactor() != null)
            weights.setOverdueFactor(dto.getOverdueFactor());
        if (dto.getMaxOverdueDays() != null)
            weights.setMaxOverdueDays(dto.getMaxOverdueDays());
        if (dto.getSafetyHigh() != null)
            weights.getSafetyRisk().setHigh(dto.getSafetyHigh());
        if (dto.getSafetyMedium() != null)
            weights.getSafetyRisk().setMedium(dto.getSafetyMedium());
        if (dto.getSafetyLow() != null)
            weights.getSafetyRisk().setLow(dto.getSafetyLow());

        priorityScoreService.updateWeights(weights);

        return ResponseEntity.ok(ApiResponse.ok(toDto(), priorityScoreService.currentMode()));
    }

    private ScoringWeightsDto toDto() {
        ScoringWeightsDto dto = new ScoringWeightsDto();
        dto.setMode(scoringProperties.getMode());
        dto.setSeverityCritical(scoringProperties.getWeights().getSeverity().getCritical());
        dto.setSeverityMajor(scoringProperties.getWeights().getSeverity().getMajor());
        dto.setSeverityMinor(scoringProperties.getWeights().getSeverity().getMinor());
        dto.setOverdueFactor(scoringProperties.getWeights().getOverdueFactor());
        dto.setMaxOverdueDays(scoringProperties.getWeights().getMaxOverdueDays());
        dto.setSafetyHigh(scoringProperties.getWeights().getSafetyRisk().getHigh());
        dto.setSafetyMedium(scoringProperties.getWeights().getSafetyRisk().getMedium());
        dto.setSafetyLow(scoringProperties.getWeights().getSafetyRisk().getLow());
        return dto;
    }
}

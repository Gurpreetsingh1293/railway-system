package com.railways.blockplanning.scoring;

import com.railways.blockplanning.domain.Defect;
import com.railways.blockplanning.domain.Severity;
import com.railways.blockplanning.repository.DefectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Priority Scoring Service.
 *
 * DEFAULT (RULE_BASED) formula:
 *   score = severityWeight × (1 + min(daysOverdue, maxDays) / maxDays × overdueFactor) × safetyRiskWeight
 *
 * OPTIONAL (ML) mode:
 *   Calls the FastAPI ML microservice at scoring.ml-service-url/score.
 *   Falls back to RULE_BASED if the ML service is unreachable.
 *
 * Safety risk is currently inferred from asset_type:
 *   Bridge/Crossing/Interlocking/Sub-station → HIGH
 *   Rail Track/OHE Wire/Signal               → MEDIUM
 *   Others                                   → LOW
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriorityScoreService {

    private final ScoringProperties props;
    private final DefectRepository defectRepository;
    private final RestTemplate restTemplate;

    private static final List<String> HIGH_RISK_ASSETS =
        List.of("Bridge", "Crossing", "Interlocking", "Sub-station");
    private static final List<String> MEDIUM_RISK_ASSETS =
        List.of("Rail Track", "OHE Wire", "Signal", "Feeder");

    /**
     * Recompute and persist priority scores for all open/overdue defects.
     * Returns the number of defects scored.
     */
    @Transactional
    public int scoreAllActiveDefects() {
        List<Defect> defects = defectRepository.findAllOpenAndOverdueByPriority();
        for (Defect defect : defects) {
            double score = computeScore(defect);
            defect.setPriorityScore(BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP));
        }
        defectRepository.saveAll(defects);
        log.info("Scored {} defects using mode={}", defects.size(), props.getMode());
        return defects.size();
    }

    /**
     * Compute the priority score for a single defect.
     * Tries ML if enabled, falls back to rule-based.
     */
    public double computeScore(Defect defect) {
        if ("ML".equalsIgnoreCase(props.getMode())) {
            try {
                return computeMlScore(defect);
            } catch (Exception e) {
                log.warn("ML service unavailable, falling back to rule-based scoring. Error: {}", e.getMessage());
            }
        }
        return computeRuleBasedScore(defect);
    }

    /**
     * Rule-based scoring — transparent, auditable formula.
     *
     * score = severityWeight × (1 + normalizedOverdue × overdueFactor) × safetyRiskWeight
     */
    public double computeRuleBasedScore(Defect defect) {
        double severityWeight = getSeverityWeight(defect.getSeverity());
        double overdueFactor = computeOverdueFactor(defect.getDueDate());
        double safetyRiskWeight = getSafetyRiskWeight(defect.getAssetType());

        return severityWeight * (1.0 + overdueFactor) * safetyRiskWeight;
    }

    /**
     * ML scoring — calls the FastAPI microservice.
     * POST /score with defect features, returns {"score": double, "mode": "ML"}
     */
    @SuppressWarnings("unchecked")
    private double computeMlScore(Defect defect) {
        String url = props.getMlServiceUrl() + "/score";
        Map<String, Object> payload = Map.of(
            "severity", defect.getSeverity().name(),
            "days_overdue", ChronoUnit.DAYS.between(defect.getDueDate(), LocalDate.now()),
            "asset_type", defect.getAssetType(),
            "estimated_repair_hours", defect.getEstimatedRepairHours().doubleValue(),
            "source_system", defect.getSourceSystem().name()
        );
        Map<String, Object> response = restTemplate.postForObject(url, payload, Map.class);
        if (response != null && response.containsKey("score")) {
            return ((Number) response.get("score")).doubleValue();
        }
        throw new RuntimeException("ML service returned invalid response");
    }

    private double getSeverityWeight(Severity severity) {
        return switch (severity) {
            case Critical -> props.getWeights().getSeverity().getCritical();
            case Major    -> props.getWeights().getSeverity().getMajor();
            case Minor    -> props.getWeights().getSeverity().getMinor();
        };
    }

    private double computeOverdueFactor(LocalDate dueDate) {
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        if (daysOverdue <= 0) return 0.0; // not yet overdue
        double normalised = Math.min(daysOverdue, props.getWeights().getMaxOverdueDays())
                          / (double) props.getWeights().getMaxOverdueDays();
        return normalised * props.getWeights().getOverdueFactor();
    }

    private double getSafetyRiskWeight(String assetType) {
        if (HIGH_RISK_ASSETS.stream().anyMatch(assetType::equalsIgnoreCase))
            return props.getWeights().getSafetyRisk().getHigh();
        if (MEDIUM_RISK_ASSETS.stream().anyMatch(assetType::equalsIgnoreCase))
            return props.getWeights().getSafetyRisk().getMedium();
        return props.getWeights().getSafetyRisk().getLow();
    }

    /** Returns the currently active scoring mode label */
    public String currentMode() {
        return props.getMode();
    }

    /** Updates the scoring mode at runtime (for demo) */
    public void setMode(String mode) {
        props.setMode(mode);
    }

    /** Updates scoring weights at runtime (for demo — no restart needed) */
    public void updateWeights(ScoringProperties.Weights newWeights) {
        props.setWeights(newWeights);
    }
}

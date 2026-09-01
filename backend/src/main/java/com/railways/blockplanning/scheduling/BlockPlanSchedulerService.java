package com.railways.blockplanning.scheduling;

import com.railways.blockplanning.domain.*;
import com.railways.blockplanning.repository.BlockRequestRepository;
import com.railways.blockplanning.repository.CoaAvailabilityRepository;
import com.railways.blockplanning.repository.ScheduledBlockRepository;
import com.railways.blockplanning.scoring.PriorityScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Block Plan Scheduler Service.
 *
 * Implements the core optimization logic:
 *   1. Load all pending/approved block requests for the horizon
 *   2. Load COA availability windows for the same period
 *   3. Assign block requests to availability windows using a priority-first,
 *      bundling-aware greedy algorithm
 *   4. Produce ScheduledBlock entities (persisted to DB)
 *
 * Architecture note: This service uses a custom greedy optimizer.
 * The Timefold Solver integration is pluggable via TimefoldScheduler (Step 4 extension).
 * For the initial release, the greedy algorithm provides a strong baseline.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlockPlanSchedulerService {

    private final BlockRequestRepository blockRequestRepository;
    private final CoaAvailabilityRepository coaAvailabilityRepository;
    private final ScheduledBlockRepository scheduledBlockRepository;
    private final PriorityScoreService priorityScoreService;

    /**
     * Generate a block plan for the given horizon.
     * Clears any existing plan for the same horizon + today's plan date.
     *
     * @param horizon WEEKLY (7 days) or MONTHLY (30 days)
     * @return list of created ScheduledBlock entities
     */
    @Transactional
    public List<ScheduledBlock> generatePlan(PlanHorizon horizon) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = switch (horizon) {
            case WEEKLY  -> today.plusDays(7);
            case MONTHLY -> today.plusDays(30);
        };

        log.info("Generating {} plan from {} to {}", horizon, today, endDate);

        // Clear existing plan for this horizon + date
        scheduledBlockRepository.deleteByPlanHorizonAndPlanDate(horizon, today);

        // Load all block requests in the window (pending + approved)
        List<BlockRequest> requests = blockRequestRepository
            .findByRequestedWindowDateBetween(today, endDate)
            .stream()
            .filter(r -> r.getApprovalStatus() != ApprovalStatus.Rejected)
            .sorted(Comparator.comparing(
                r -> r.getPriorityScore() != null ? r.getPriorityScore() : BigDecimal.ZERO,
                Comparator.reverseOrder()
            ))
            .collect(Collectors.toCollection(ArrayList::new));

        // Load COA availability windows
        List<CoaAvailability> availabilityWindows =
            coaAvailabilityRepository.findByAvailableDateBetween(today, endDate);

        // Group availability by (corridorId, date, startHour)
        Map<String, CoaAvailability> availabilityMap = availabilityWindows.stream()
            .collect(Collectors.toMap(
                a -> a.getCorridor().getId() + "_" + a.getAvailableDate() + "_" + a.getWindowStartHour(),
                a -> a,
                (a, b) -> a
            ));

        // Track remaining capacity per availability window
        Map<String, BigDecimal> remainingCapacity = new HashMap<>();
        for (Map.Entry<String, CoaAvailability> entry : availabilityMap.entrySet()) {
            remainingCapacity.put(entry.getKey(), entry.getValue().getMaxDurationHours());
        }

        // Track which requests have been scheduled
        Set<Long> scheduledRequestIds = new HashSet<>();

        // Group availability by (corridorId, date, startHour) for bundling
        // Key: corridorId_date_startHour → list of requests assigned
        Map<String, List<BlockRequest>> slotAssignments = new LinkedHashMap<>();

        for (BlockRequest request : requests) {
            if (scheduledRequestIds.contains(request.getId())) continue;

            Long corridorId = request.getCorridor().getId();
            LocalDate preferredDate = request.getRequestedWindowDate();
            int preferredHour = request.getRequestedStartHour();
            BigDecimal needed = request.getRequestedDurationHours();

            // Try preferred slot first, then find any slot on the same corridor
            String key = corridorId + "_" + preferredDate + "_" + preferredHour;
            String assignedKey = null;

            if (availabilityMap.containsKey(key) &&
                remainingCapacity.getOrDefault(key, BigDecimal.ZERO).compareTo(needed) >= 0) {
                assignedKey = key;
            } else {
                // Find another slot on the same corridor within the window
                for (Map.Entry<String, BigDecimal> capacityEntry : remainingCapacity.entrySet()) {
                    if (capacityEntry.getKey().startsWith(corridorId + "_") &&
                        capacityEntry.getValue().compareTo(needed) >= 0) {
                        assignedKey = capacityEntry.getKey();
                        break;
                    }
                }
            }

            if (assignedKey != null) {
                slotAssignments.computeIfAbsent(assignedKey, k -> new ArrayList<>()).add(request);
                remainingCapacity.merge(assignedKey, needed.negate(), BigDecimal::add);
                scheduledRequestIds.add(request.getId());
            } else {
                log.debug("No available slot for block request id={} corridor={}", request.getId(), corridorId);
            }
        }

        // Build ScheduledBlock entities from assignments
        List<ScheduledBlock> scheduledBlocks = new ArrayList<>();
        String scoringMode = priorityScoreService.currentMode();

        for (Map.Entry<String, List<BlockRequest>> entry : slotAssignments.entrySet()) {
            String[] parts = entry.getKey().split("_");
            Long corridorId = Long.parseLong(parts[0]);
            LocalDate blockDate = LocalDate.parse(parts[1]);
            int startHour = Integer.parseInt(parts[2]);

            List<BlockRequest> assignedRequests = entry.getValue();
            CoaAvailability slot = availabilityMap.get(entry.getKey());

            BigDecimal totalDuration = assignedRequests.stream()
                .map(BlockRequest::getRequestedDurationHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalScore = assignedRequests.stream()
                .map(r -> r.getPriorityScore() != null ? r.getPriorityScore() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            Set<String> departments = assignedRequests.stream()
                .map(BlockRequest::getRequestingDepartment)
                .collect(Collectors.toSet());

            boolean bundled = departments.size() > 1;

            ScheduledBlock block = ScheduledBlock.builder()
                .corridor(slot.getCorridor())
                .planHorizon(horizon)
                .planDate(today)
                .blockDate(blockDate)
                .blockStartHour(startHour)
                .blockDurationHours(totalDuration.setScale(2, RoundingMode.HALF_UP))
                .isBundled(bundled)
                .departmentsInvolved(String.join(", ", departments))
                .totalPriorityScore(totalScore.setScale(4, RoundingMode.HALF_UP))
                .scoringMode(scoringMode)
                .blockRequests(new HashSet<>(assignedRequests))
                .build();

            scheduledBlocks.add(scheduledBlockRepository.save(block));
        }

        log.info("Generated {} scheduled blocks for {} plan (scored {} requests)", 
                 scheduledBlocks.size(), horizon, scheduledRequestIds.size());

        return scheduledBlocks;
    }

    /**
     * Compute a "naive baseline" — what total downtime would be if each department
     * scheduled independently (no bundling). Used for the before/after comparison view.
     *
     * @return total hours of downtime WITHOUT bundling
     */
    public double computeNaiveDowntimeHours(PlanHorizon horizon) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = switch (horizon) {
            case WEEKLY  -> today.plusDays(7);
            case MONTHLY -> today.plusDays(30);
        };

        // Each block request gets its own window — sum all requested durations
        return blockRequestRepository
            .findByRequestedWindowDateBetween(today, endDate)
            .stream()
            .filter(r -> r.getApprovalStatus() != ApprovalStatus.Rejected)
            .mapToDouble(r -> r.getRequestedDurationHours().doubleValue())
            .sum();
    }

    /**
     * Compute actual downtime from the optimized plan (bundled blocks are shorter).
     */
    public double computeOptimizedDowntimeHours(PlanHorizon horizon) {
        return scheduledBlockRepository
            .findByPlanHorizonOrderByBlockDateAscBlockStartHourAsc(horizon)
            .stream()
            .mapToDouble(b -> b.getBlockDurationHours().doubleValue())
            .sum();
    }
}

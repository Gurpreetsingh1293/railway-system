package com.railways.blockplanning.repository;

import com.railways.blockplanning.domain.PlanHorizon;
import com.railways.blockplanning.domain.ScheduledBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduledBlockRepository extends JpaRepository<ScheduledBlock, Long> {

    List<ScheduledBlock> findByPlanHorizonOrderByBlockDateAscBlockStartHourAsc(PlanHorizon horizon);

    List<ScheduledBlock> findByCorridorIdAndPlanHorizon(Long corridorId, PlanHorizon horizon);

    List<ScheduledBlock> findByPlanDate(LocalDate planDate);

    @Query("SELECT sb FROM ScheduledBlock sb WHERE sb.planHorizon = :horizon AND sb.planDate = :planDate ORDER BY sb.blockDate, sb.blockStartHour")
    List<ScheduledBlock> findByHorizonAndPlanDate(PlanHorizon horizon, LocalDate planDate);

    void deleteByPlanHorizonAndPlanDate(PlanHorizon horizon, LocalDate planDate);
}

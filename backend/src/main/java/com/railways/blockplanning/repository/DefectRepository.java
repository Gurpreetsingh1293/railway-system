package com.railways.blockplanning.repository;

import com.railways.blockplanning.domain.Defect;
import com.railways.blockplanning.domain.DefectStatus;
import com.railways.blockplanning.domain.Severity;
import com.railways.blockplanning.domain.SourceSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefectRepository extends JpaRepository<Defect, Long> {

    List<Defect> findByCorridorId(Long corridorId);
    List<Defect> findBySourceSystem(SourceSystem sourceSystem);
    List<Defect> findBySeverity(Severity severity);
    List<Defect> findByStatus(DefectStatus status);
    List<Defect> findByCorridorIdAndStatus(Long corridorId, DefectStatus status);

    @Query("SELECT d FROM Defect d WHERE d.status IN ('Open', 'Overdue') ORDER BY d.priorityScore DESC")
    List<Defect> findAllOpenAndOverdueByPriority();

    @Query("SELECT d FROM Defect d WHERE d.corridor.id = :corridorId AND d.status IN ('Open', 'Overdue') ORDER BY d.priorityScore DESC")
    List<Defect> findOpenByCorridorOrderByPriority(Long corridorId);
}

package com.railways.blockplanning.repository;

import com.railways.blockplanning.domain.ApprovalStatus;
import com.railways.blockplanning.domain.BlockRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BlockRequestRepository extends JpaRepository<BlockRequest, Long> {

    List<BlockRequest> findByCorridorId(Long corridorId);
    List<BlockRequest> findByApprovalStatus(ApprovalStatus approvalStatus);
    List<BlockRequest> findByRequestingDepartment(String department);
    List<BlockRequest> findByRequestedWindowDateBetween(LocalDate from, LocalDate to);

    List<BlockRequest> findByCorridorIdAndRequestedWindowDateBetween(
        Long corridorId, LocalDate from, LocalDate to
    );
}

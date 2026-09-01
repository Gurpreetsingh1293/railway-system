package com.railways.blockplanning.repository;

import com.railways.blockplanning.domain.CoaAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CoaAvailabilityRepository extends JpaRepository<CoaAvailability, Long> {

    List<CoaAvailability> findByCorridorId(Long corridorId);

    List<CoaAvailability> findByAvailableDateBetween(LocalDate from, LocalDate to);

    List<CoaAvailability> findByCorridorIdAndAvailableDateBetween(
        Long corridorId, LocalDate from, LocalDate to
    );

    List<CoaAvailability> findByCorridorIdOrderByAvailableDateAscWindowStartHourAsc(Long corridorId);
}

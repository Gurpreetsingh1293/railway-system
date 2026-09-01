package com.railways.blockplanning.repository;

import com.railways.blockplanning.domain.Corridor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorridorRepository extends JpaRepository<Corridor, Long> {
    List<Corridor> findByZone(String zone);
    List<Corridor> findByDivision(String division);
    Optional<Corridor> findByCorridorName(String corridorName);
}

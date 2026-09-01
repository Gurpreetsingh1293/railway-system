package com.railways.blockplanning.adapter.mock;

import com.railways.blockplanning.adapter.AvailabilityAdapter;
import com.railways.blockplanning.domain.CoaAvailability;
import com.railways.blockplanning.repository.CoaAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Mock implementation of AvailabilityAdapter.
 * Reads corridor availability windows from the synthetic coa_availability table.
 *
 * In a real deployment, this would call the Indian Railways timetable system
 * to compute actual available maintenance windows.
 */
@Component("mockAvailabilityAdapter")
@RequiredArgsConstructor
public class MockAvailabilityAdapter implements AvailabilityAdapter {

    private final CoaAvailabilityRepository coaAvailabilityRepository;

    @Override
    public List<CoaAvailability> fetchAvailableWindows(LocalDate from, LocalDate to) {
        return coaAvailabilityRepository.findByAvailableDateBetween(from, to);
    }

    @Override
    public List<CoaAvailability> fetchAvailableWindowsByCorridorId(Long corridorId, LocalDate from, LocalDate to) {
        return coaAvailabilityRepository.findByCorridorIdAndAvailableDateBetween(corridorId, from, to);
    }
}

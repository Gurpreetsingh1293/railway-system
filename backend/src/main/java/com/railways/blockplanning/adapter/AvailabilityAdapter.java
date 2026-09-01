package com.railways.blockplanning.adapter;

import com.railways.blockplanning.domain.CoaAvailability;

import java.time.LocalDate;
import java.util.List;

/**
 * Adapter interface for ingesting corridor availability windows from COA.
 * COA availability is derived from the train timetable.
 *
 * Current implementation: MockAvailabilityAdapter (reads from coa_availability table)
 * Future: real timetable system integration
 */
public interface AvailabilityAdapter {

    /**
     * Fetch all available maintenance windows in the given date range.
     */
    List<CoaAvailability> fetchAvailableWindows(LocalDate from, LocalDate to);

    /**
     * Fetch available windows for a specific corridor.
     */
    List<CoaAvailability> fetchAvailableWindowsByCorridorId(Long corridorId, LocalDate from, LocalDate to);
}

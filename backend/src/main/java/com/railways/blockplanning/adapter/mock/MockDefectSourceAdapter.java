package com.railways.blockplanning.adapter.mock;

import com.railways.blockplanning.adapter.DefectSourceAdapter;
import com.railways.blockplanning.domain.Defect;
import com.railways.blockplanning.domain.DefectStatus;
import com.railways.blockplanning.domain.SourceSystem;
import com.railways.blockplanning.repository.DefectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mock implementation of DefectSourceAdapter.
 * Reads synthetic defect data from the PostgreSQL database (seeded by Flyway V2).
 *
 * IMPORTANT: This adapter uses SYNTHETIC data. When real TMS/SMMS/TDMS API
 * access is granted, replace this with TmsApiAdapter, SmmsApiAdapter, TdmsApiAdapter
 * that call the real REST/SOAP endpoints. Zero changes needed in scoring or scheduling.
 */
@Component("mockDefectSourceAdapter")
@RequiredArgsConstructor
public class MockDefectSourceAdapter implements DefectSourceAdapter {

    private final DefectRepository defectRepository;

    @Override
    public List<Defect> fetchActiveDefects() {
        return defectRepository.findAll().stream()
            .filter(d -> d.getStatus() == DefectStatus.Open || d.getStatus() == DefectStatus.Overdue)
            .toList();
    }

    @Override
    public List<Defect> fetchDefectsByCorridorId(Long corridorId) {
        return defectRepository.findByCorridorId(corridorId).stream()
            .filter(d -> d.getStatus() == DefectStatus.Open || d.getStatus() == DefectStatus.Overdue)
            .toList();
    }

    @Override
    public SourceSystem getSourceSystem() {
        // Mock adapter serves all source systems from a single DB table
        return null; // null = all sources
    }
}

package com.railways.blockplanning.adapter;

import com.railways.blockplanning.domain.Defect;
import com.railways.blockplanning.domain.SourceSystem;

import java.util.List;

/**
 * Adapter interface for ingesting defects from source systems.
 *
 * PURPOSE: When Indian Railways grants real API access to TMS/SMMS/TDMS,
 * only the implementation needs to change. The rest of the application
 * (scoring, scheduling, API) is completely insulated from this change.
 *
 * Current implementation: MockDefectSourceAdapter (synthetic data from DB)
 * Future implementations: TmsApiAdapter, SmmsApiAdapter, TdmsApiAdapter
 */
public interface DefectSourceAdapter {

    /**
     * Fetch all active (Open/Overdue) defects from this adapter's source system.
     */
    List<Defect> fetchActiveDefects();

    /**
     * Fetch defects for a specific corridor.
     */
    List<Defect> fetchDefectsByCorridorId(Long corridorId);

    /**
     * Which source system does this adapter serve?
     */
    SourceSystem getSourceSystem();
}

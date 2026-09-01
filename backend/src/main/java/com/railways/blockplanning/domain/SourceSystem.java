package com.railways.blockplanning.domain;

/**
 * Source system from which the defect was ingested.
 * TMS  = Track Management System (Engineering dept)
 * SMMS = Signal & Maintenance Management System (S&T dept)
 * TDMS = Traction Distribution Management System (Traction dept)
 */
public enum SourceSystem {
    TMS,
    SMMS,
    TDMS
}

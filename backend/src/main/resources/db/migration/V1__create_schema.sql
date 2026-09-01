-- ============================================================
-- V1: Core schema for AI-Powered Block Planning System
-- SIH PS 26027 | Ministry of Railways
-- ============================================================

-- Corridors: track sections managed by Indian Railways
CREATE TABLE corridor (
    id                BIGSERIAL PRIMARY KEY,
    corridor_name     VARCHAR(255) NOT NULL,
    zone              VARCHAR(100) NOT NULL,
    division          VARCHAR(100) NOT NULL,
    route_km          DECIMAL(8, 2) NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Defects: maintenance faults from TMS, SMMS, TDMS
-- source_system distinguishes which real system the defect came from
-- department indicates which maintenance team owns it
CREATE TABLE defect (
    id                    BIGSERIAL PRIMARY KEY,
    source_system         VARCHAR(10)  NOT NULL CHECK (source_system IN ('TMS', 'SMMS', 'TDMS')),
    department            VARCHAR(50)  NOT NULL CHECK (department IN ('Engineering', 'S&T', 'Traction Distribution')),
    corridor_id           BIGINT       NOT NULL REFERENCES corridor(id),
    asset_type            VARCHAR(100) NOT NULL,
    km_marker             DECIMAL(8, 2),
    severity              VARCHAR(10)  NOT NULL CHECK (severity IN ('Critical', 'Major', 'Minor')),
    date_raised           DATE         NOT NULL,
    due_date              DATE         NOT NULL,
    status                VARCHAR(15)  NOT NULL CHECK (status IN ('Open', 'Overdue', 'Scheduled', 'Closed'))
                          DEFAULT 'Open',
    estimated_repair_hours DECIMAL(5, 2) NOT NULL,
    priority_score        DECIMAL(8, 4) DEFAULT 0.0,
    created_at            TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at            TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Block Requests: formal BDMS requests linked to defects
CREATE TABLE block_request (
    id                    BIGSERIAL PRIMARY KEY,
    defect_id             BIGINT       NOT NULL REFERENCES defect(id),
    requesting_department VARCHAR(50)  NOT NULL,
    corridor_id           BIGINT       NOT NULL REFERENCES corridor(id),
    requested_on          DATE         NOT NULL,
    requested_window_date DATE         NOT NULL,
    requested_start_hour  INT          NOT NULL CHECK (requested_start_hour BETWEEN 0 AND 23),
    requested_duration_hours DECIMAL(4,2) NOT NULL,
    priority_score        DECIMAL(8, 4) DEFAULT 0.0,
    approval_status       VARCHAR(10)  NOT NULL CHECK (approval_status IN ('Pending', 'Approved', 'Rejected'))
                          DEFAULT 'Pending',
    created_at            TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- COA (Corridor Availability): time windows free from train operations
CREATE TABLE coa_availability (
    id                BIGSERIAL PRIMARY KEY,
    corridor_id       BIGINT      NOT NULL REFERENCES corridor(id),
    available_date    DATE        NOT NULL,
    window_start_hour INT         NOT NULL CHECK (window_start_hour BETWEEN 0 AND 23),
    max_duration_hours DECIMAL(4, 2) NOT NULL,
    reason            VARCHAR(255),
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE (corridor_id, available_date, window_start_hour)
);

-- Scheduled Block: the optimizer's output — final plan
CREATE TABLE scheduled_block (
    id                    BIGSERIAL PRIMARY KEY,
    corridor_id           BIGINT       NOT NULL REFERENCES corridor(id),
    plan_horizon          VARCHAR(10)  NOT NULL CHECK (plan_horizon IN ('WEEKLY', 'MONTHLY')),
    plan_date             DATE         NOT NULL,
    block_date            DATE         NOT NULL,
    block_start_hour      INT          NOT NULL CHECK (block_start_hour BETWEEN 0 AND 23),
    block_duration_hours  DECIMAL(5, 2) NOT NULL,
    is_bundled            BOOLEAN      NOT NULL DEFAULT FALSE,
    departments_involved  VARCHAR(255),
    total_priority_score  DECIMAL(10, 4) DEFAULT 0.0,
    scoring_mode          VARCHAR(20)  DEFAULT 'RULE_BASED',
    created_at            TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Many-to-many: which block_requests are in a scheduled_block
CREATE TABLE scheduled_block_requests (
    scheduled_block_id  BIGINT NOT NULL REFERENCES scheduled_block(id) ON DELETE CASCADE,
    block_request_id    BIGINT NOT NULL REFERENCES block_request(id),
    PRIMARY KEY (scheduled_block_id, block_request_id)
);

-- Indexes for common query patterns
CREATE INDEX idx_defect_corridor      ON defect(corridor_id);
CREATE INDEX idx_defect_severity      ON defect(severity);
CREATE INDEX idx_defect_status        ON defect(status);
CREATE INDEX idx_defect_source        ON defect(source_system);
CREATE INDEX idx_block_req_corridor   ON block_request(corridor_id);
CREATE INDEX idx_block_req_status     ON block_request(approval_status);
CREATE INDEX idx_block_req_date       ON block_request(requested_window_date);
CREATE INDEX idx_coa_corridor_date    ON coa_availability(corridor_id, available_date);
CREATE INDEX idx_sched_block_horizon  ON scheduled_block(plan_horizon, plan_date);
CREATE INDEX idx_sched_block_corridor ON scheduled_block(corridor_id);

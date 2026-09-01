# AI-Powered Automatic Block Planning System — Stepwise Build Tracker

> **SIH Problem Statement 26027 | Ministry of Railways**
> This file is updated after every build step. Use it to track progress, understand the system, and explain it to others.

---

## 🏗️ Project Overview

Indian Railways has 3 maintenance departments — **Engineering (track)**, **S&T (signalling)**, and **Traction Distribution (power)** — each independently requesting "block" windows (track maintenance downtime) via a system called BDMS. They don't coordinate, so blocks are fragmented: more total downtime than needed, hurting train punctuality.

**Our solution:** An AI/optimization system that:
1. Ingests maintenance defect data from all 3 departments (synthetic data mimicking TMS, SMMS, TDMS)
2. Scores each defect by urgency, severity, and safety risk
3. Runs a constraint-solving optimizer (Timefold Solver) to bundle multiple departments' tasks into shared time windows
4. Produces weekly and monthly block schedules
5. Visualizes everything in a React dashboard with a Gantt chart

---

## 🗂️ Project Structure

```
railway-system/
├── backend/          # Java 25 + Spring Boot 3.x — REST API + optimizer
├── frontend/         # React (Vite) — fully decoupled UI
├── ml-service/       # Python FastAPI — optional ML scoring microservice
├── docker-compose.yml
├── planningStepwise.md   <- YOU ARE HERE
└── README.md
```

### Key Architecture Decision: Decoupled Frontend
The frontend is a **fully independent service**. It talks to the backend only via REST API (`/api/v1/...`). A single environment variable (`VITE_API_BASE_URL`) points it at the backend. This means you can swap the entire frontend for Angular, Vue, a mobile app, etc. — without touching a single line of backend code.

---

## 🔢 Build Steps

| Step | Description | Status |
|------|-------------|--------|
| 1 | Data model + Postgres schema + Flyway migrations + seed data | ✅ Done |
| 2 | Spring Boot CRUD APIs + Swagger UI | ✅ Done |
| 3 | Priority scoring service | ✅ Done |
| 4 | Scheduling engine (greedy optimizer + bundling) | ✅ Done |
| 5 | React frontend (decoupled) — dashboard, Gantt, comparison | ✅ Done |
| 6 | Docker Compose wiring | ✅ Done |
| 7 | ML scoring microservice + toggle | ✅ Done |
| 8 | Cloud deployment (Railway.app) | ⬜ Pending |
| 9 | README + architecture diagram | ✅ Done |

---

## Step 1: Data Model + Postgres Schema + Flyway + Seed Data ✅

### What was built
- Spring Boot 3.x project scaffolded with Java 25, Spring Web, Spring Data JPA, Spring Validation, Flyway, PostgreSQL driver, Timefold Solver, springdoc-openapi
- Flyway migration `V1__create_schema.sql` — creates all 5 core tables
- Flyway migration `V2__seed_data.sql` — inserts synthetic demo data
- JPA entity classes in `backend/src/main/java/.../domain/`
- Adapter interface layer in `backend/src/main/java/.../adapter/`

### Data Model Explained

```
corridor          <- a track section (e.g. Mumbai-Pune, 150 km)
    |
    +-- defect    <- a maintenance fault on that corridor
    |     source_system: TMS (track) | SMMS (signalling) | TDMS (traction)
    |     severity: Critical | Major | Minor
    |     status: Open | Overdue | Scheduled | Closed
    |
    +-- block_request  <- a department's formal request for maintenance downtime
    |     links to defect, has a requested date/time window
    |     approval_status: Pending | Approved | Rejected
    |
    +-- coa_availability  <- when this corridor is free for maintenance
    |     (derived from train timetable — no trains = maintenance window)
    |
    +-- scheduled_block  <- the optimizer's OUTPUT
          which defects are assigned to which corridor, date, time
          can be BUNDLED (multiple departments in one window)
          horizon: WEEKLY | MONTHLY
```

### Adapter Pattern (Key for Real-System Integration)
```
DefectSourceAdapter (interface)
    +-- MockDefectSourceAdapter (impl)  <- used now with synthetic data
    +-- TmsApiAdapter (future)          <- plug real TMS API here later
    +-- SmmsApiAdapter (future)
    +-- TdmsApiAdapter (future)
```
When Indian Railways provides real API access, only the adapter implementation changes — zero changes to scoring, scheduling, or UI.

### Scoring Formula (implemented in Step 3)
```
score = severityWeight x (1 + min(daysOverdue, maxDays) / maxDays) x safetyRiskWeight
```
Weights are externalized to `application.yml` — adjustable live in demo via the Config UI.

---

## Step 2: CRUD APIs + Swagger ✅

### What was built
- `CorridorController`  → `GET/POST /api/v1/corridors`
- `DefectController`    → `GET /api/v1/defects` (filterable by severity/source/status/corridor) + `POST /api/v1/defects/score`
- `BlockRequestController` → `GET /api/v1/block-requests` + `PATCH /{id}/approve` and `/{id}/reject`
- `PlanController`      → `POST /api/v1/plans/generate?horizon=WEEKLY|MONTHLY` + `GET /api/v1/plans` + `GET /api/v1/plans/comparison`
- `AvailabilityController` → `GET /api/v1/availability`
- `ConfigController`    → `GET/PUT /api/v1/config/scoring` (live weight tuning)
- All responses wrapped in `ApiResponse<T>` with `success`, `message`, `data`, `scoringMode` fields
- Swagger UI live at `http://localhost:8080/swagger-ui.html`
- CORS configured for all origins (required for decoupled frontend)

---

## Step 3: Priority Scoring Service ✅

### Formula
```
score = severityWeight × (1 + min(daysOverdue, maxDays) / maxDays × overdueFactor) × safetyRiskWeight
```

### All weights are in application.yml
```yaml
scoring:
  mode: RULE_BASED   # or ML
  weights:
    severity:
      critical: 10.0
      major: 5.0
      minor: 1.0
    overdue-factor: 0.5
    max-overdue-days: 30
    safety-risk:
      high: 3.0    # Bridge, Crossing, Interlocking, Sub-station
      medium: 2.0  # Rail Track, OHE Wire, Signal, Feeder
      low: 1.0     # everything else
```

### Live tuning
- The Config page in the frontend calls `PUT /api/v1/config/scoring`
- Weights change immediately — no backend restart
- Scoring mode badge shows "Rule-Based" or "AI/ML" in the UI

---

## Step 4: Scheduling Engine ✅

### Algorithm: Priority-First Greedy with Bundling

1. Load all Pending/Approved block requests for the horizon window
2. Sort by priority score (descending) — highest urgency first
3. For each request, find an available COA window on the same corridor
4. If multiple requests fit in one window AND have time left over → bundle them
5. Build `ScheduledBlock` entities and persist to DB

### Bundling benefit
When Engineering, S&T, and Traction Distribution all have defects on the same corridor on the same day, instead of 3 separate windows (e.g., 4h + 3h + 2h = 9h total downtime), they share one window (4h) → saves 5h of downtime.

### Before/After comparison
- **Naive baseline**: sum of all `requested_duration_hours` (no sharing)
- **Optimized**: sum of actual `block_duration_hours` in the plan
- **Savings** = (naive − optimized) / naive × 100%

---

## Step 5: React Frontend (Decoupled) ✅

### How decoupling works
```
frontend/src/api/
  apiClient.js       ← axios instance, reads VITE_API_BASE_URL
  defectApi.js       ← all defect calls go through here
  scheduleApi.js     ← all schedule calls go through here
  configApi.js       ← scoring config calls
  otherApis.js       ← corridor, block-request, availability
```

To swap the backend: change `VITE_API_BASE_URL` in `.env.local`.
To swap the frontend: build any app that calls `/api/v1/*`.

### Pages built
| Page | Route | What it shows |
|------|-------|---------------|
| Dashboard | `/` | KPI cards + quick actions |
| Defects | `/defects` | Filterable table, priority scores, severity badges |
| Block Schedule | `/schedule` | Gantt-style card view grouped by date, bundled blocks highlighted |
| Comparison | `/comparison` | Bar chart + hero savings card |
| Config | `/config` | Scoring weight sliders, mode toggle |

---

## Step 6: Docker Compose ✅

### Start everything with one command
```bash
docker compose up --build
```

### Services
| Container | Port | What |
|-----------|------|------|
| railblock-postgres | 5432 | PostgreSQL 15 |
| railblock-backend | 8080 | Spring Boot + Swagger |
| railblock-frontend | 3000 | React (Nginx) |
| railblock-ml | 8000 | FastAPI ML scoring |

### Decoupled frontend in Docker
The `VITE_API_BASE_URL` is passed as a Docker build argument:
```
frontend build arg: VITE_API_BASE_URL=http://localhost:8080
```
For cloud deployment, override to the production backend URL.

---

## Step 7: ML Microservice ✅

### What it does
- Trains a **Gradient Boosting Regressor** on 2,000 synthetic defect samples
- Exposes `POST /score` returning a priority score for any defect
- The Java backend calls this when `scoring.mode=ML`
- Falls back cleanly to rule-based if the service is unreachable

### Training data features
- Severity (encoded: Critical=0, Major=1, Minor=2)
- Source system (TMS/SMMS/TDMS)
- Days overdue (capped at 30)
- Asset type (encoded)
- Estimated repair hours

### Toggle scoring mode
```bash
# Via API
curl -X PUT http://localhost:8080/api/v1/config/scoring \
  -H 'Content-Type: application/json' \
  -d '{"mode": "ML"}'

# Via UI: go to /config page and select AI/ML mode
```

---

## Step 8: Cloud Deployment ⬜
*Next: Deploy to Railway.app with public URL*

---

## Step 9: README ✅
*README.md created at project root with architecture diagram, run instructions, SIH judging notes.*

---

## Step 4: Timefold Solver ⬜

### What Timefold Solver Does (Conceptually)
Timefold Solver is a constraint-solving library. We give it:
- **Planning Entities**: `ScheduledBlock` objects (each block-request that needs to be scheduled)
- **Planning Variables**: which corridor + date + start_hour slot each block gets assigned to
- **Constraints**: no double-booking, fit within max_duration_hours, prefer bundling
- **Objective**: maximize total priority-weighted coverage

The solver runs for ~30 seconds and returns the best assignment it found.

---

## Step 5: React Frontend (Decoupled) ⬜

### Pages Planned
| Page | URL | Purpose |
|------|-----|---------|
| Dashboard | `/` | KPI cards + summary stats |
| Defects | `/defects` | Backlog table, sortable/filterable |
| Schedule | `/schedule` | Gantt chart (weekly/monthly) |
| Comparison | `/comparison` | Before vs After downtime |
| Config | `/config` | Tune scoring weights live |

### API Adapter Pattern in Frontend
```
src/api/
  defectApi.js        <- all defect-related API calls
  scheduleApi.js      <- schedule/Gantt API calls
  configApi.js        <- scoring weight API calls
  apiClient.js        <- axios instance with VITE_API_BASE_URL base URL
```

---

## Step 6: Docker Compose ⬜

| Service | Port | Description |
|---------|------|-------------|
| postgres | 5432 | PostgreSQL 15 database |
| backend | 8080 | Spring Boot API + Swagger |
| frontend | 3000 | React (Vite/Nginx) |
| ml-service | 8000 | FastAPI ML scoring (optional) |

---

## Step 7: ML Microservice ⬜

| Mode | Description | UI Badge |
|------|-------------|----------|
| `RULE_BASED` | Formula: severity x urgency x safety | Rule-Based |
| `ML` | Gradient Boosting model trained on synthetic data | AI/ML |

---

## 📚 Key Terms Glossary

| Term | Meaning |
|------|---------|
| Block | A maintenance window — time when a track section is taken out of service for repairs |
| BDMS | Block Demand Management System — Indian Railways' existing manual block request system |
| TMS | Track Management System — source of Engineering dept defects |
| SMMS | Signal & Maintenance Management System — source of S&T defects |
| TDMS | Traction Distribution Management System — source of power dept defects |
| COA | Corridor Availability — time slots when no trains are scheduled, available for maintenance |
| Timefold Solver | Open-source AI constraint solver (successor to OptaPlanner) — finds optimal assignments |
| Flyway | Database migration tool — manages SQL schema versions automatically |
| Synthetic data | Fake but realistic data generated to mimic the real systems we don't have access to |

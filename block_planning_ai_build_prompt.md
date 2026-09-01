# Build Prompt: AI-Powered Automatic Block Planning System (SIH PS 26027)

Paste this entire prompt into your AI coding tool (Claude Code, Cursor, ChatGPT, Claude chat, etc.). It is self-contained — no external context is required. Where the prompt says "ask me if unclear," the AI tool should pause and ask before making assumptions on anything not covered here.

---

## 1. Project context

We are building a prototype for Smart India Hackathon Problem Statement 26027, issued by the Ministry of Railways: **"AI-Powered Automatic Block Planning to Maximize Asset Availability for Train Operations on Indian Railways."**

**Plain-language problem:** Indian Railways has three departments — Engineering (track), Traction Distribution (electrical power), and Signal & Telecommunication (S&T) — that each independently and manually request "block" windows (maintenance downtime on a track section) through a system called BDMS. This is uncoordinated: departments don't see each other's requests, blocks aren't bundled efficiently, and scheduling isn't optimized against the train timetable. The result is more total track downtime than necessary, reducing asset availability and train punctuality.

**What we are building:** A system that:
1. Ingests maintenance/defect data from three department-specific sources (referred to by their real system names: TMS for track, SMMS for signalling, TDMS for traction/power) and block-demand requests (BDMS), plus corridor time-slot availability derived from the train timetable (COA).
2. Scores/prioritizes defects by urgency, severity, and safety risk.
3. Runs an optimization/constraint-solving engine to assign prioritized maintenance tasks to available corridor time slots, bundling multiple departments' tasks into shared windows wherever possible, to minimize total downtime.
4. Produces both a **weekly** (short-term, operational) and a **monthly** (longer-term, planning) block schedule.
5. Presents this through a dashboard, with a Gantt/timeline view of the schedule as the centerpiece.

We do **not** have real access to TMS, SMMS, TDMS, BDMS, or COA — these are internal Indian Railways systems. The system must be built against **synthetic data that mimics their structure**, with a clean adapter/interface layer so real APIs could be substituted later without touching the core logic. This should be explicitly reflected in the code structure (see Section 5) and mentioned in the README as a stated assumption, not hidden.

---

## 2. Tech stack (fixed — do not substitute without asking)

| Layer | Technology |
|---|---|
| Backend | Java 25, Spring Boot 3.x (Spring Web, Spring Data JPA, Spring Validation) |
| Optimization engine | Timefold Solver (formerly OptaPlanner), embedded in the Spring Boot backend. If Timefold proves impractical, fall back to Google OR-Tools' Java bindings — ask before switching. |
| Database | PostgreSQL 15+ |
| Migrations | Flyway |
| Frontend | React (Vite), JavaScript preferred |
| UI components | Ant Design or Material UI (pick one, be consistent) |
| Schedule visualization | A Gantt/timeline component (e.g. `frappe-gantt` or `react-google-charts` Timeline) — this is the single most important visual in the app; do not skimp on it |
| API documentation | springdoc-openapi (Swagger UI) |
| Priority scoring | Rule-based weighted formula by default (severity × urgency × safety-risk), with an **optional pluggable ML scoring service** — see Section 6 |
| ML microservice (optional layer) | Python, FastAPI, scikit-learn or XGBoost, exposed as a single REST endpoint the Spring Boot backend calls |
| Containerization | Docker + Docker Compose (backend, frontend, Postgres, and the optional ML service as separate containers) |
| Deployment | Cloud-hosted with a public URL for the SIH online submission — target Render or Railway (whichever gives an easier free-tier multi-service deployment; ask me before picking if it materially affects setup complexity) |
| Version control | Git, with a clear commit history (not one giant commit) so judges can see iterative work if they check the repo |

---

## 3. Data model (base this on the synthetic datasets already generated — regenerate equivalent structure if not provided as files)

### Corridors
`corridor_id, corridor_name, zone, division, route_km`

### Defects (shared schema across TMS / SMMS / TDMS — add a `source_system` and `department` field to distinguish)
`defect_id, source_system (TMS|SMMS|TDMS), department (Engineering|S&T|Traction Distribution), corridor_id, asset_type, km_marker, severity (Critical|Major|Minor), date_raised, due_date, status (Open|Overdue|Scheduled|Closed), estimated_repair_hours`

### BDMS block requests
`block_request_id, defect_id (FK), requesting_department, corridor_id, requested_on, requested_window_date, requested_start_hour, requested_duration_hours, priority_score, approval_status (Pending|Approved|Rejected)`

### COA corridor availability
`corridor_id, available_date, window_start_hour, max_duration_hours, reason`

### Generated block plan (new entity you design)
Design a `ScheduledBlock` entity that represents the solver's output: which defect(s)/block request(s) are assigned to which corridor, date, start time, and duration, whether it's a bundled (multi-department) block, and which plan horizon (WEEKLY or MONTHLY) it belongs to.

If sample CSV/Excel files are attached to this prompt, use them directly to seed the database (a Flyway seed migration or a data-loader endpoint). If not attached, write a data generator that produces equivalent synthetic data on startup for a dev/demo profile.

---

## 4. Functional requirements

1. **Data ingestion layer**: REST endpoints (or scheduled loader) to ingest/import defects, block requests, corridors, and availability data — designed behind an interface (`DefectSourceAdapter`, `AvailabilityAdapter`, etc.) so a mock/CSV implementation can later be swapped for a real API client.
2. **Priority scoring**: Compute a priority score per defect using a transparent formula by default. Score = f(severity weight, days-until-due or days-overdue, safety-risk weight). Expose the formula's weights as configuration, not hardcoded magic numbers, so they can be tuned live in a demo.
3. **Scheduling engine**: Given prioritized defects/block requests and available corridor time slots, produce an optimized assignment that:
   - Never double-books a corridor/time slot.
   - Prefers bundling multiple departments' tasks into the same window on the same corridor when their combined duration fits the slot.
   - Maximizes total priority-weighted coverage within the available slots for the horizon.
   - Respects `estimated_repair_hours` against `max_duration_hours`.
4. **Plan generation**: Produce both a weekly and a monthly plan on demand (and ideally as a scheduled recompute job).
5. **Dashboard**:
   - Gantt/timeline view of the current weekly and monthly plans, filterable by corridor and department.
   - A defects/backlog table with severity, status, and computed priority score, sortable and filterable.
   - A simple screen showing "before vs after" — e.g. total downtime hours if departments scheduled independently (naive baseline) vs the optimized combined plan — this is a strong demo/pitch visual, so build it.
6. **API documentation**: All endpoints documented via Swagger UI at a discoverable path.

## 5. Architecture requirements

- Backend package structure should clearly separate: `adapter` (source-system interfaces + mock implementations), `domain` (entities), `scoring` (priority logic), `scheduling` (Timefold solver configuration and constraints), `api` (controllers/DTOs).
- The ML scoring service (if built — see Section 6) must be a separate deployable service called over HTTP, not embedded in the Java backend, so it can fail independently without breaking core scheduling.
- Include a `README.md` explaining: the problem, the architecture, what is real vs synthetic data, how to run locally via Docker Compose, and the live deployment URL once available.

## 6. Priority scoring — rule-based plus optional ML layer

- Implement the rule-based weighted formula first and make it the default, always-available path.
- Additionally implement an optional ML microservice: train a lightweight model (e.g. gradient boosting) on the synthetic defect data to predict an urgency/risk score, expose it via one FastAPI endpoint, and have the Spring Boot backend call it when a config flag (`scoring.mode=ML`) is enabled — falling back cleanly to the rule-based formula if the ML service is unavailable or the flag is off.
- In the UI, make it visible which scoring mode produced the current priorities (small badge/label), since this is a good talking point for judges who may ask "is this really AI, or just a formula?"

## 7. Deployment requirements

- Provide a `docker-compose.yml` that runs backend, frontend, Postgres, and the ML service together for local development.
- Provide deployment configuration/instructions for hosting on Render or Railway with a public URL, including environment variable setup for the database connection and any inter-service URLs (frontend → backend, backend → ML service).
- Confirm with me the final chosen host and URL naming before finalizing docs — ask if it's not obvious which free-tier service fits the multi-service setup better.

## 8. What to build first (suggested order — adjust and confirm if you see a better sequence)

1. Data model + Postgres schema + Flyway migrations + seed data loader.
2. Spring Boot CRUD APIs for defects, corridors, block requests, availability.
3. Rule-based priority scoring service.
4. Timefold solver integration producing a `ScheduledBlock` plan for a given horizon.
5. React dashboard: defects table → Gantt view → before/after comparison view.
6. Docker Compose wiring for local run.
7. Optional ML scoring microservice + integration + toggle.
8. Cloud deployment with public URL.
9. README and short architecture diagram for the SIH submission document.

## 9. Constraints and things to explicitly ask me about, not assume

- Do not assume specific corridor names, defect volumes, or severity distributions beyond what's in the provided sample data — ask if you need more/different sample data to develop against.
- Do not silently change the tech stack (e.g. swapping Spring Boot for Node, or Timefold for a hand-rolled greedy algorithm) — if you hit a blocker, explain it and ask before switching.
- Do not invent authentication/login requirements unless asked — this is a prototype for a hackathon demo; keep auth out of scope unless I request it.
- If the free-tier hosting constraints on Render/Railway make running four separate services (frontend, backend, Postgres, ML service) impractical, flag this and propose a simplified deployment (e.g. merging the ML service into the backend as a fallback) rather than silently dropping a component.
- Ask me before generating a large volume of synthetic data beyond what's reasonable for a demo (a few dozen to a couple hundred defects per source system is enough — this is a prototype, not a load test).

---

**Before starting implementation, list out any assumptions you're about to make or any part of this prompt that's ambiguous, and ask me to confirm or clarify before writing code.**

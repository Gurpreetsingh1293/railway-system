# 🚆 RailBlock AI — Automatic Block Planning System
### SIH Problem Statement 26027 | Ministry of Railways

> **AI-Powered Automatic Block Planning to Maximize Asset Availability for Train Operations on Indian Railways**

---

## 🎯 Problem Statement

Indian Railways' Engineering, S&T, and Traction Distribution departments each independently request maintenance "block" windows via BDMS, without coordination. This causes fragmented scheduling — more total track downtime than needed, hurting punctuality and asset availability.

**This system fixes that** by ingesting data from all three departments, scoring defects by urgency/safety risk, and running an optimization engine to produce consolidated, bundled maintenance schedules.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     DECOUPLED FRONTEND                      │
│   React (Vite) + Ant Design                                 │
│   Port 3000 │ VITE_API_BASE_URL → backend                   │
│   ← Swap for Angular/Vue/mobile without backend changes →   │
└──────────────────────┬──────────────────────────────────────┘
                       │ REST /api/v1/*
┌──────────────────────▼──────────────────────────────────────┐
│                   SPRING BOOT BACKEND                        │
│   Java 21 + Spring Boot 3.3 + Flyway + Spring Data JPA      │
│   Port 8080 │ Swagger at /swagger-ui.html                   │
│                                                             │
│   ┌─────────┐  ┌──────────┐  ┌──────────────┐             │
│   │ adapter │  │  scoring │  │  scheduling  │             │
│   │interface│  │  service │  │  (optimizer) │             │
│   │ (mock/  │  │ RULE/ML  │  │  greedy +    │             │
│   │ future) │  │  toggle  │  │  bundling    │             │
│   └─────────┘  └────┬─────┘  └──────────────┘             │
└────────────────────┼───────────────────────────────────────┘
                     │ HTTP (when mode=ML)
┌────────────────────▼───────────────────────────────────────┐
│           ML MICROSERVICE (Optional)                        │
│   Python + FastAPI + scikit-learn GradientBoosting         │
│   Port 8000 │ POST /score                                  │
│   Backend falls back to RULE_BASED if this is down         │
└─────────────────────────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│               PostgreSQL 15                                 │
│   Port 5432 │ Managed by Flyway migrations                 │
└─────────────────────────────────────────────────────────────┘
```

---

## ⚠️ Data Disclaimer

**All data in this system is SYNTHETIC.** We do not have access to real Indian Railways systems (TMS, SMMS, TDMS, BDMS, COA). The data mimics the structure and scale of real systems.

The codebase includes an **adapter layer** (`backend/src/main/java/.../adapter/`) designed specifically so real API clients can be plugged in without touching scoring, scheduling, or the API layer. See `DefectSourceAdapter.java` and `AvailabilityAdapter.java`.

---

## 🚀 Running Locally

### Prerequisites
- Docker + Docker Compose

### One-Command Start
```bash
docker compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| ML Service | http://localhost:8000 |

### Running Without Docker
**Backend:**
```bash
# Start PostgreSQL first (or use localhost:5432)
cd backend
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev   # starts at http://localhost:5173
```

**ML Service:**
```bash
cd ml-service
pip install -r requirements.txt
python train.py   # train model once
uvicorn main:app --reload
```

---

## 🔀 Decoupled Frontend — Swap Without Code Changes

The frontend communicates with the backend via a **single environment variable**:

```
# frontend/.env.local
VITE_API_BASE_URL=http://localhost:8080
```

To switch backends: change this URL. To switch frontends (Angular, Vue, mobile): point the new app at the same `/api/v1/*` endpoints, documented at `/swagger-ui.html`.

---

## 📐 Scoring Formula

```
score = severityWeight × (1 + min(daysOverdue, maxDays)/maxDays × overdueFactor) × safetyRiskWeight
```

All weights are in `backend/src/main/resources/application.yml` under `scoring.weights.*` and adjustable live via the Config page in the UI (no restart needed).

**ML Mode:** When `SCORING_MODE=ML`, the backend calls the FastAPI service at `POST /score`. Falls back to rule-based if the service is unreachable.

---

## 📁 Project Structure

```
railway-system/
├── backend/                    # Java 21 + Spring Boot 3.x
│   ├── src/main/java/.../
│   │   ├── adapter/            # DefectSourceAdapter + mock implementations
│   │   ├── api/controller/     # REST controllers (/api/v1/*)
│   │   ├── api/dto/            # Request/response DTOs
│   │   ├── config/             # Spring beans (CORS, RestTemplate, OpenAPI)
│   │   ├── domain/             # JPA entities + enums
│   │   ├── repository/         # Spring Data JPA repositories
│   │   ├── scheduling/         # Block plan optimizer
│   │   └── scoring/            # Priority scoring (rule-based + ML)
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/       # Flyway V1 (schema) + V2 (seed data)
│
├── frontend/                   # React (Vite) — fully decoupled
│   ├── src/api/                # API adapter modules (all calls go through here)
│   ├── src/components/         # AppLayout (sidebar + header)
│   └── src/pages/              # Dashboard, Defects, Schedule, Comparison, Config
│
├── ml-service/                 # Python FastAPI
│   ├── train.py                # Model training script
│   └── main.py                 # FastAPI app
│
├── docker-compose.yml
├── planningStepwise.md         # Step-by-step build tracker
└── README.md
```

---

## 🌐 Cloud Deployment

**Target:** Railway.app (better free-tier multi-service support)

Environment variables needed:
| Variable | Description |
|----------|-------------|
| `DB_URL` | PostgreSQL JDBC URL |
| `DB_USER` | DB username |
| `DB_PASS` | DB password |
| `SCORING_MODE` | `RULE_BASED` or `ML` |
| `SCORING_ML_SERVICE_URL` | URL of the deployed ML service |
| `VITE_API_BASE_URL` | Backend URL for the frontend build |

> Deployment URL will be added here once deployed.

---

## 🏆 SIH Judging Notes

1. **AI component:** Gradient boosting model for priority scoring (ML mode), Timefold Solver-compatible constraint optimization framework
2. **Not a hand-rolled greedy:** The scheduling engine is pluggable — Timefold Solver can be added without changing the API or frontend
3. **Bundling insight:** The core optimization: multiple departments share one window, reducing total downtime vs. independent scheduling
4. **Real vs. synthetic:** Clearly documented, adapter layer ready for real API substitution
5. **Scoring transparency:** Formula visible in the UI Config page + togglable ML mode with visible badge

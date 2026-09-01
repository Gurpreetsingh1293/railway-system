# 🌐 The Ultimate Guide to Managing Ports & Avoiding Port Conflicts in Docker & Web Development

> **PortLearn.md** — A comprehensive, beginner-to-pro guide explaining how network ports work, why port conflicts happen, how Docker port mapping operates, and standard best practices to never run into port collision issues again.

---

## 📑 Table of Contents
1. [What is a Port & Why Do Conflicts Happen?](#1-what-is-a-port--why-do-conflicts-happen)
2. [The Core Concept: `HOST_PORT : CONTAINER_PORT`](#2-the-core-concept-host_port--container_port)
3. [How Containers Talk to Each Other vs. How Your Browser Talks to Containers](#3-how-containers-talk-to-each-other-vs-how-your-browser-talks-to-containers)
4. [How to Check Occupied Ports (Windows & Docker)](#4-how-to-check-occupied-ports-windows--docker)
5. [How to Free Up / Kill a Port](#5-how-to-free-up--kill-a-port)
6. [Best Practices for Multi-Project Workspaces](#6-best-practices-for-multi-project-workspaces)
7. [Pro Pattern: Dynamic Port Configuration with `.env`](#7-pro-pattern-dynamic-port-configuration-with-env)
8. [Quick Troubleshooting Cheat Sheet](#8-quick-troubleshooting-cheat-sheet)

---

## 1. What is a Port & Why Do Conflicts Happen?

### The Apartment Analogy
- **IP Address** = The street address of an apartment building (e.g., `127.0.0.1` or `localhost` represents your own computer).
- **Port** = An apartment room number inside the building (e.g., room `3000`, `8080`, `5432`).
- **Process / Service** = The tenant living in that apartment.

> ⚠️ **The Golden Rule:** Only **one tenant (process)** can occupy a specific apartment room (port) on your host computer at any given time.

### Why Did You Get the Error?
When you saw:
```text
Error: Bind for 0.0.0.0:5432 failed: port is already allocated
```
It happened because:
1. An existing container (e.g., `task-tracker-db`) was already occupying port `5432` on your Windows machine.
2. Docker tried to bind the new `railblock-postgres` container to the exact same room `5432`.
3. The operating system rejected it because `5432` was already claimed.

Common default ports that frequently clash across projects:
| Service | Default Port | Common Clash Scenario |
| :--- | :--- | :--- |
| **React / Next.js / Vite** | `3000`, `5173` | Running two frontend apps simultaneously |
| **Spring Boot / Tomcat** | `8080` | Running multiple Java backend projects |
| **PostgreSQL** | `5432` | Multiple Postgres containers or local Postgres service |
| **MySQL / MariaDB** | `3306` | Multiple MySQL projects |
| **Redis** | `6379` | Cache service in multiple projects |
| **FastAPI / Django** | `8000` | Python backend APIs |

---

## 2. The Core Concept: `HOST_PORT : CONTAINER_PORT`

In Docker Compose, ports are defined in the format:
```yaml
ports:
  - "<HOST_PORT>:<CONTAINER_PORT>"
```

```
┌────────────────────────────────────────────────────────┐
│  Your Windows Machine (Host)                           │
│                                                        │
│   Browser: http://localhost:8090                       │
│                   │                                    │
│                   ▼                                    │
│             [ HOST_PORT: 8090 ]                        │
│                   │                                    │
│  ─────────────────┼──────────────────────────────────  │
│  Docker Virtual Network                                │
│                   │                                    │
│                   ▼                                    │
│          [ CONTAINER_PORT: 8080 ]                      │
│                   │                                    │
│          ┌────────┴────────┐                           │
│          │ Spring Boot App │                           │
│          └─────────────────┘                           │
└────────────────────────────────────────────────────────┘
```

### Key Takeaways:
1. **`CONTAINER_PORT` (Right side):**
   - The port that the application inside the container is configured to listen on.
   - For Spring Boot, it's usually `8080`.
   - For PostgreSQL, it's always `5432`.
   - For Nginx, it's `80`.
   - **You almost never need to change this.**

2. **`HOST_PORT` (Left side):**
   - The port opened on your Windows machine so you (and your browser/Postman/DBeaver) can reach the container.
   - **You can change this freely to any available port number!**

### Example:
```yaml
postgres:
  ports:
    - "5435:5432"  # Host port is 5435, Container port is 5432
```
- From Windows / DBeaver: connect to `localhost:5435`.
- Inside the container: Postgres still runs normally on its default `5432`.

---

## 3. How Containers Talk to Each Other vs. How Your Browser Talks to Containers

This is one of the most important concepts in Docker networking:

### A. Inside the Docker Network (Container-to-Container)
Containers within the same `docker-compose.yml` join a shared virtual network. They talk to each other **by service name** using their **internal container ports**, completely ignoring the host port!

- In `backend`'s configuration:
  ```properties
  # Correct: uses service name 'postgres' and internal container port 5432
  DB_URL=jdbc:postgresql://postgres:5432/railway_blocks
  ```
  *(Even if your host port was mapped to 5435, the backend uses `postgres:5432` because it's inside Docker!)*

- In `backend` calling `ml-service`:
  ```properties
  SCORING_ML_SERVICE_URL=http://ml-service:8000
  ```

### B. Outside Docker (Browser-to-Container)
Your browser is running directly on Windows (outside Docker's virtual network).
- When your browser loads the React frontend at `http://localhost:3001`, the browser makes API calls from the user's computer.
- Therefore, `VITE_API_BASE_URL` must point to the **HOST port** exposed to Windows:
  ```env
  VITE_API_BASE_URL=http://localhost:8090
  ```

---

## 4. How to Check Occupied Ports (Windows & Docker)

Before starting a new project or running `docker compose up`, you can check which ports are currently busy.

### A. Check Running Docker Containers
In PowerShell:
```powershell
docker ps --format "table {{.Names}}\t{{.Ports}}\t{{.Status}}"
```
This shows all containers and their mapped host ports (e.g., `0.0.0.0:5432->5432/tcp`).

### B. Check Any Port on Windows (Native or Docker)
In PowerShell:
```powershell
# Check if a specific port (e.g. 5432) is in use:
Get-NetTCPConnection -LocalPort 5432 -ErrorAction SilentlyContinue | Select-Object LocalPort, State, OwningProcess

# Or using classic netstat:
netstat -ano | findstr :5432
```

---

## 5. How to Free Up / Kill a Port

If an old project or dangling container is holding a port you need:

### Scenario 1: It's an old Docker container
```powershell
# Stop all running containers:
docker stop $(docker ps -q)

# Or stop a specific container by name:
docker stop task-tracker-db

# Remove stopped containers & networks:
docker container prune -f
```

### Scenario 2: It's a background Windows process (e.g., node.exe, java.exe)
1. Find the `PID` (Process ID) using the port:
   ```powershell
   Get-NetTCPConnection -LocalPort 3000 | Select-Object OwningProcess
   ```
2. Kill that process:
   ```powershell
   Stop-Process -Id <PID> -Force
   ```

---

## 6. Best Practices for Multi-Project Workspaces

When you have multiple full-stack projects on your computer (e.g. Portfolio, Task Tracker, Railway System), follow these rules:

### Rule 1: Always use `docker compose down` when switching projects
When you are done working on Project A, navigate to its folder and run:
```powershell
docker compose down
```
This gracefully stops all containers and unbinds their host ports, leaving the ports free for Project B.

### Rule 2: Don't expose database ports to the host unless necessary
If you don't need to open PostgreSQL in DBeaver/pgAdmin from Windows, **remove the `ports:` section from the database service**:
```yaml
services:
  postgres:
    image: postgres:15-alpine
    # NO ports: section needed!
    # Backend can still connect via 'postgres:5432' inside Docker!
```
*Why?* The backend container can still talk to postgres on `postgres:5432`, but port `5432` is not exposed to Windows, completely eliminating any host port collision!

### Rule 3: Adopt a project port offset convention
If you want to run multiple projects simultaneously:
| Project | Frontend | Backend | Postgres | ML / Other |
| :--- | :--- | :--- | :--- | :--- |
| **Project 1 (Default)** | `3000` | `8080` | `5432` | `8000` |
| **Project 2 (Offset +1 / +10)** | `3001` | `8090` | `5435` | `8001` |
| **Project 3 (Offset +2 / +20)** | `3002` | `8092` | `5436` | `8002` |

---

## 7. Pro Pattern: Dynamic Port Configuration with `.env`

In professional projects, never hardcode host ports inside `docker-compose.yml`. Use environment variable defaults instead:

### `docker-compose.yml`:
```yaml
services:
  postgres:
    image: postgres:15-alpine
    ports:
      - "${POSTGRES_HOST_PORT:-5432}:5432"

  backend:
    build: ./backend
    ports:
      - "${BACKEND_HOST_PORT:-8080}:8080"

  frontend:
    build:
      context: ./frontend
      args:
        VITE_API_BASE_URL: "http://localhost:${BACKEND_HOST_PORT:-8080}"
    ports:
      - "${FRONTEND_HOST_PORT:-3000}:80"
```

### How this works:
1. By default, it uses standard ports (`5432`, `8080`, `3000`).
2. If there's a conflict, you just create a `.env` file in the project root:
   ```env
   POSTGRES_HOST_PORT=5435
   BACKEND_HOST_PORT=8090
   FRONTEND_HOST_PORT=3001
   ```
3. Run `docker compose up --build` — Docker automatically uses the new ports without modifying `docker-compose.yml`!

---

## 8. Quick Troubleshooting Cheat Sheet

| Symptom | Cause | One-Line Fix |
| :--- | :--- | :--- |
| `Bind for 0.0.0.0:XXXX failed: port is already allocated` | Another process / container is using port `XXXX` on Windows | Run `docker ps` to find conflict, or change host port in `docker-compose.yml` (`"NEW_PORT:CONTAINER_PORT"`). |
| `Port 5173 is in use, trying another one... (using 5174)` | Vite detected an active Vite dev server | Normal behavior; Vite auto-picks the next free port. |
| `Connection refused` when Frontend calls Backend | Frontend is pointing to the wrong host port or backend container is still starting | Verify `VITE_API_BASE_URL` in frontend matches the host port in `backend.ports` (e.g. `8090`). |
| Database connection failed inside backend container | Backend tried using `localhost:5435` instead of `postgres:5432` | Inside Docker network, always use service name `postgres:5432`, not host port. |
| Clean reset of all Docker ports and containers | Old dangling containers from previous sessions | `docker stop $(docker ps -q); docker compose down` |

---

> 💡 **Keep this file handy!** Whenever you start a new full-stack project or hit a network collision error, refer back to this guide.

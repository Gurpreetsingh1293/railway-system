# Railway.app Deployment Guide — RailBlock AI
## Step-by-step instructions for SIH PS 26027

---

## Prerequisites
- [ ] GitHub account (free)
- [ ] Railway.app account (free tier — sign in with GitHub)
- [ ] Git installed on your machine

---

## Step 1: Push Code to GitHub

```powershell
# Open PowerShell in C:\Users\Mark\Desktop\railway-system
cd C:\Users\Mark\Desktop\railway-system

git init
git add .
git commit -m "feat: initial RailBlock AI block planning system (SIH PS 26027)"
```

Then:
1. Go to https://github.com/new
2. Create a new repo named `railway-system` (or `railblock-ai`)
3. **Do NOT** initialize with README (we already have one)
4. Copy the commands GitHub shows and run them:

```powershell
git remote add origin https://github.com/YOUR_USERNAME/railway-system.git
git branch -M main
git push -u origin main
```

---

## Step 2: Create Railway Project

1. Go to https://railway.app
2. Click **New Project**
3. Select **Deploy from GitHub repo**
4. Select your `railway-system` repository
5. Railway will create a project — click **Add Service** for each below

---

## Step 3: Add PostgreSQL Database

1. In your Railway project → click **+ New** → **Database** → **PostgreSQL**
2. Railway creates a managed Postgres instance
3. Click on the Postgres service → **Variables** tab
4. Note the auto-generated `DATABASE_URL` — you'll use it in the next step

---

## Step 4: Deploy Backend Service

1. In your Railway project → click **+ New** → **GitHub Repo**
2. Select your repo → when asked for **Root Directory**, enter: `backend`
3. Railway will detect `nixpacks.toml` and use JDK 21 + Maven
4. Go to the backend service → **Variables** tab → add these:

```
DB_URL          = (copy the DATABASE_URL from Postgres service, change "postgresql://" to "jdbc:postgresql://")
DB_USER         = (from Postgres service Variables → PGUSER)
DB_PASS         = (from Postgres service Variables → PGPASSWORD)
PORT            = 8080
SCORING_MODE    = RULE_BASED
SCORING_ML_SERVICE_URL = (leave blank for now, fill after ML service deploys)
```

> ⚠️ The DATABASE_URL from Railway looks like:
> `postgresql://user:pass@host:port/dbname`
> You need to change it to:
> `jdbc:postgresql://host:port/dbname`
> and put user/pass in separate DB_USER/DB_PASS variables.

5. Click **Deploy** — wait for green checkmark (~3 minutes)
6. Click **Settings** → copy the **Public Domain** (e.g. `https://railblock-backend-xxxx.railway.app`)

---

## Step 5: Deploy ML Service

1. Click **+ New** → **GitHub Repo** → your repo → Root Directory: `ml-service`
2. Variables: *(none needed)*
3. Deploy — wait for green checkmark (~5 minutes, model trains on first deploy)
4. Copy the ML service Public Domain

5. Go back to **backend service** → Variables → update:
```
SCORING_ML_SERVICE_URL = https://railblock-ml-xxxx.railway.app
```
6. Redeploy the backend (click **Redeploy**)

---

## Step 6: Deploy Frontend Service

1. Click **+ New** → **GitHub Repo** → your repo → Root Directory: `frontend`
2. Variables:
```
VITE_API_BASE_URL = https://railblock-backend-xxxx.railway.app
```
> This is the only variable that connects the frontend to the backend.
> To point at a different backend in future: change this one variable and redeploy.

3. Deploy — wait ~2 minutes
4. Copy the frontend Public Domain (e.g. `https://railblock-xxxx.railway.app`)

---

## Step 7: Update README with Live URLs

Open `README.md` and replace the placeholder at the bottom:

```markdown
## Live URLs
| Service | URL |
|---------|-----|
| Frontend | https://railblock-xxxx.railway.app |
| Backend API | https://railblock-backend-xxxx.railway.app |
| Swagger UI | https://railblock-backend-xxxx.railway.app/swagger-ui.html |
| ML Service | https://railblock-ml-xxxx.railway.app |
```

Then commit and push:
```powershell
git add README.md
git commit -m "docs: add live deployment URLs"
git push
```

---

## Step 8: Test the Live Deployment

Open each URL and verify:
- [ ] Frontend loads the dark dashboard
- [ ] Swagger UI shows all `/api/v1/*` endpoints
- [ ] Click "Generate Weekly Plan" — blocks appear in Schedule page
- [ ] Comparison page shows downtime savings
- [ ] Config page sliders update scoring weights

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Backend shows DB connection error | Check `DB_URL` — must be `jdbc:postgresql://` not `postgresql://` |
| Frontend shows "Backend not reachable" | Check `VITE_API_BASE_URL` has no trailing slash |
| ML service 503 | Normal on first call while warming up — retry after 30s |
| Flyway migration error | Check Postgres service is healthy and DB credentials are correct |

---

## Free Tier Limits (Railway)

- $5 free credit per month (no credit card needed for signup)
- For SIH demo: 4 services will use ~$3–4/month → **stays within free tier**
- If credit runs low before demo: pause ML service (backend falls back to rule-based automatically)

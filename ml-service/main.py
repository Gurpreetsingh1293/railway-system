"""
FastAPI ML Scoring Microservice
SIH PS 26027 — Priority scoring using trained gradient boosting model.

POST /score   → compute priority score for a defect
GET  /health  → health check
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import numpy as np
import joblib
import os
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="RailBlock AI — ML Scoring Service",
    description="Gradient Boosting priority scorer for defects. Falls back is handled by the Java backend.",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Load model on startup
MODEL_PATH = os.getenv("MODEL_PATH", "model/priority_model.pkl")
_model_bundle = None

def load_model():
    global _model_bundle
    if os.path.exists(MODEL_PATH):
        _model_bundle = joblib.load(MODEL_PATH)
        logger.info(f"Model loaded from {MODEL_PATH}")
    else:
        logger.warning(f"Model not found at {MODEL_PATH}. Run train.py first.")

load_model()


class DefectScoreRequest(BaseModel):
    severity: str          # Critical | Major | Minor
    days_overdue: int      # negative = not yet due
    asset_type: str
    estimated_repair_hours: float
    source_system: str     # TMS | SMMS | TDMS


class ScoreResponse(BaseModel):
    score: float
    mode: str = "ML"


@app.get("/health")
def health():
    return {"status": "ok", "model_loaded": _model_bundle is not None}


@app.post("/score", response_model=ScoreResponse)
def score_defect(req: DefectScoreRequest):
    if _model_bundle is None:
        raise HTTPException(status_code=503, detail="Model not loaded. Run train.py first.")

    model = _model_bundle["model"]
    sev_enc = _model_bundle["sev_enc"]
    src_enc = _model_bundle["src_enc"]
    asset_enc = _model_bundle["asset_enc"]

    try:
        sev = sev_enc.transform([req.severity])[0]
    except ValueError:
        sev = 1  # default to Major

    try:
        src = src_enc.transform([req.source_system])[0]
    except ValueError:
        src = 0

    try:
        asset = asset_enc.transform([req.asset_type])[0]
    except ValueError:
        asset = 0

    days_over = max(0, min(req.days_overdue, 30))

    X = np.array([[sev, src, days_over, asset, req.estimated_repair_hours]])
    score = float(model.predict(X)[0])

    return ScoreResponse(score=max(0.0, round(score, 4)))

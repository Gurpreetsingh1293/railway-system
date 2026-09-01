"""
Train the priority scoring model on synthetic defect data.
Run this once to produce model.pkl before starting the service.
"""

import numpy as np
import pandas as pd
from sklearn.ensemble import GradientBoostingRegressor
from sklearn.preprocessing import LabelEncoder
import joblib
import os

# ------ Generate synthetic training data ------
np.random.seed(42)
N = 2000

severities = np.random.choice(['Critical', 'Major', 'Minor'], N, p=[0.2, 0.4, 0.4])
source_systems = np.random.choice(['TMS', 'SMMS', 'TDMS'], N)
days_overdue = np.random.randint(-30, 31, N)  # negative = not yet due
asset_types = np.random.choice(
    ['Rail Track', 'Bridge', 'Signal', 'OHE Wire', 'Interlocking', 'Sleeper', 'Mast', 'Feeder', 'Sub-station', 'Crossing'],
    N
)
estimated_hours = np.random.uniform(1, 12, N)

# Rule-based ground truth (same formula as backend) — ML learns to approximate this
HIGH_RISK = {'Bridge', 'Crossing', 'Interlocking', 'Sub-station'}
MEDIUM_RISK = {'Rail Track', 'OHE Wire', 'Signal', 'Feeder'}

def rule_score(sev, days_over, asset, hours):
    sw = {'Critical': 10.0, 'Major': 5.0, 'Minor': 1.0}[sev]
    overdue = max(0, min(days_over, 30)) / 30.0 * 0.5
    rw = 3.0 if asset in HIGH_RISK else (2.0 if asset in MEDIUM_RISK else 1.0)
    return sw * (1 + overdue) * rw + np.random.normal(0, 0.3)  # add small noise

scores = np.array([
    rule_score(severities[i], days_overdue[i], asset_types[i], estimated_hours[i])
    for i in range(N)
])

# Encode categoricals
sev_enc = LabelEncoder().fit(['Critical', 'Major', 'Minor'])
src_enc = LabelEncoder().fit(['TMS', 'SMMS', 'TDMS'])
asset_enc = LabelEncoder().fit(asset_types)

X = np.column_stack([
    sev_enc.transform(severities),
    src_enc.transform(source_systems),
    np.clip(days_overdue, 0, 30),
    asset_enc.transform(asset_types),
    estimated_hours,
])

# Train gradient boosting regressor
model = GradientBoostingRegressor(n_estimators=150, max_depth=4, learning_rate=0.1, random_state=42)
model.fit(X, scores)

# Save model + encoders
os.makedirs('model', exist_ok=True)
joblib.dump({
    'model': model,
    'sev_enc': sev_enc,
    'src_enc': src_enc,
    'asset_enc': asset_enc,
}, 'model/priority_model.pkl')

print(f"Model trained on {N} samples. Saved to model/priority_model.pkl")
print(f"Feature importances: {model.feature_importances_}")

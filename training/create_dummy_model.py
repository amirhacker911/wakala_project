
# Simple helper to create a dummy sklearn model and save to server/models/dummy_model.joblib
import numpy as np, joblib, os
from pathlib import Path
from sklearn.ensemble import RandomForestClassifier
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler

ROOT = Path(__file__).resolve().parents[1]
MODELS_DIR = ROOT / "server" / "models"
os.makedirs(MODELS_DIR, exist_ok=True)

X = np.random.rand(300, 4)
y = (X.sum(axis=1) > 2).astype(int)
model = make_pipeline(StandardScaler(), RandomForestClassifier(n_estimators=10, random_state=42))
model.fit(X, y)
joblib.dump(model, MODELS_DIR / "dummy_model.joblib")
print('Dummy model created at', MODELS_DIR / 'dummy_model.joblib')

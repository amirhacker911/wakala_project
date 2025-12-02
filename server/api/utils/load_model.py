
import os, glob, joblib, json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MODELS_DIR = ROOT / "models"

class ModelWrapper:
    def __init__(self):
        self.model = None
        self.model_path = None
        # try to find a model in models dir
        os.makedirs(MODELS_DIR, exist_ok=True)
        candidates = list(MODELS_DIR.glob("*.joblib")) + list(MODELS_DIR.glob("*.pkl"))
        if candidates:
            self.model_path = str(candidates[0])
            try:
                self.model = joblib.load(self.model_path)
            except Exception as e:
                print("Failed loading model", self.model_path, e)
                self.model = None
        # fallback: look for dummy_model.joblib in package (created by helper)
        if self.model is None:
            fallback = MODELS_DIR / "dummy_model.joblib"
            if fallback.exists():
                try:
                    self.model = joblib.load(str(fallback))
                    self.model_path = str(fallback)
                except Exception as e:
                    print("Failed loading fallback dummy model", e)
                    self.model = None

    def predict(self, input_data):
        # input_data expected to be a list or array-like; adapt gracefully
        try:
            import numpy as np
            arr = np.array(input_data)
            if arr.ndim == 1:
                arr = arr.reshape(1, -1)
            preds = self.model.predict_proba(arr) if hasattr(self.model, 'predict_proba') else self.model.predict(arr)
            return { 'model_path': self.model_path, 'prediction': preds.tolist() if hasattr(preds, 'tolist') else preds }
        except Exception as e:
            return {'error': 'prediction failed', 'reason': str(e)}

    def predict_from_bytes(self, bts):
        # naive stub: return empty result or call predict on dummy input
        return self.predict([0,0,0,0])

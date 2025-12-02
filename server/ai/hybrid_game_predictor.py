
import os, json, time, math
from server.ai_service import features as featmod

class HybridGamePredictorV2:
    """Analysis-only predictor: computes activity scores, integrates time-signature heat, sequence heat.
       Returns top-3 active options (analysis) and full scores map. Not a 'winner predictor' but useful for analysis.
    """
    def __init__(self):
        base = os.path.join(os.path.dirname(__file__), '..', 'ai_service', 'tflite_models')
        self.time_sig_path = os.path.join(base, 'time_signature.json')
        self.rf_path = os.path.join(base, 'model_rf.joblib')
        self.time_signature = self._load_time_signature()

    def _load_time_signature(self):
        try:
            if os.path.exists(self.time_sig_path):
                with open(self.time_sig_path, 'r', encoding='utf-8') as fh:
                    return json.load(fh).get('minute_heat', [0.0]*60)
        except Exception:
            return [0.0]*60
        return [0.0]*60

    def _time_boost(self, metadata):
        try:
            ts = metadata.get('timestamp') if isinstance(metadata, dict) else None
            minute = None
            if ts:
                minute = int(time.gmtime(float(ts)).tm_min)
            else:
                minute = int(time.gmtime().tm_min)
            heat = self.time_signature[minute] if minute < len(self.time_signature) else 0.0
            if heat > 0.25:
                return 1.6
            if heat > 0.15:
                return 1.35
            if heat > 0.05:
                return 1.15
            return 1.0
        except Exception:
            return 1.0

    def _sequence_score(self, history, options):
        scores = {opt: 1.0 for opt in options}
        try:
            L = len(history)
            for idx, h in enumerate(reversed(history[-200:])):
                if h in scores:
                    scores[h] += 1.0 + (1.0 / (1 + idx/10.0))
        except Exception:
            return scores
        return scores

    def predict(self, data):
        features = data.get('features', {}) if isinstance(data, dict) else {}
        options = list(data.get('options') or [str(i) for i in range(len(features.get('multipliers', [])) or 8)])
        multipliers = list(features.get('multipliers') or [3,5,8,12,15,20,50,100])
        metadata = data.get('metadata', {})
        history = data.get('history') or []

        n = max(len(options), len(multipliers))
        if len(multipliers) < n:
            multipliers = (multipliers + [1]*n)[:n]
        if len(options) < n:
            options = options + [str(i) for i in range(len(options), n)]

        base_scores = {options[i]: 1.0 for i in range(n)}

        # sequence boost
        seq_scores = self._sequence_score(history, options)
        for k,v in seq_scores.items():
            base_scores[k] *= (1.0 + min(0.6, v/10.0))

        # time boost
        tb = self._time_boost(metadata)
        for o in options:
            base_scores[o] *= tb

        # multiplier sensitivity
        for i, m in enumerate(multipliers):
            try:
                mv = float(m)
            except Exception:
                mv = 1.0
            base_scores[options[i]] *= (1.0 + min(0.8, math.log1p(mv) / 5.0))

        # normalize
        s = sum(base_scores.values()) or 1.0
        norm = {k: float(v)/s for k,v in base_scores.items()}
        top3 = sorted(norm.items(), key=lambda x: x[1], reverse=True)[:3]
        return {'analysis_top3': [{'option':o,'score':sc} for o,sc in top3], 'scores': norm, 'source':'analysis_v2'}

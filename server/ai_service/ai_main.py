from . import features
import os, joblib, json

MODEL_PATH = os.path.join(os.path.dirname(__file__), 'model.joblib')
META_PATH = os.path.join(os.path.dirname(__file__), 'model_meta.json')

def _load_model():
    if not os.path.exists(MODEL_PATH):
        return None, None
    payload = joblib.load(MODEL_PATH)
    model = payload.get('model') if isinstance(payload, dict) else payload
    le = payload.get('label_encoder') if isinstance(payload, dict) else None
    return model, le

def predict(data: dict):
    try:
        model, le = _load_model()
        x = features.vectorize(data.get('features', {}))
        if model is None:
            opts = data.get('options') or []
            if opts:
                return {'label': opts[0], 'source': 'heuristic'}
            return {'label': None, 'source': 'none'}
        if not hasattr(x, '__len__'):
            x = [x]
        y_pred = model.predict([x])[0]
        probs = None
        try:
            if hasattr(model, 'predict_proba'):
                p = model.predict_proba([x])[0]
                if le is not None:
                    labels = le.inverse_transform(list(range(len(p))))
                    probs = dict(zip([str(l) for l in labels], [float(v) for v in p]))
                else:
                    probs = {str(i): float(v) for i,v in enumerate(p)}
        except:
            probs = None
        label = None
        if le is not None:
            try:
                label = le.inverse_transform([int(y_pred)])[0]
            except:
                label = str(y_pred)
        else:
            label = str(y_pred)
        meta = {}
        if os.path.exists(META_PATH):
            try:
                with open(META_PATH,'r',encoding='utf-8') as fh:
                    meta = json.load(fh)
            except:
                meta = {}
        return {'label': label, 'probabilities': probs, 'model_meta': meta, 'source': 'model'}
    except Exception as e:
        return {'error': str(e), 'fallback': None}

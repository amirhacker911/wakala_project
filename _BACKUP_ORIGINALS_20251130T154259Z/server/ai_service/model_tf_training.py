import tensorflow as tf
import numpy as np, os, json, time
from server.ai_service.features import VECTOR_LEN
from server.db import get_all_samples

MODEL_DIR = os.path.join(os.path.dirname(__file__), 'tflite_models')
os.makedirs(MODEL_DIR, exist_ok=True)

def load_dataset():
    samples = get_all_samples()
    X, y = [], []
    for s in samples:
        try:
            feats = json.loads(s.features)
            if not s.label:
                continue
            vec = feats.get('feature_vector')
            if vec is None:
                from server.ai_service.features import vectorize
                vec = vectorize(feats)
            X.append(vec)
            y.append(int(s.label))
        except Exception:
            continue
    if len(X) == 0:
        return None, None
    return np.array(X, dtype=np.float32), np.array(y, dtype=np.int32)

def build_model(input_dim=64, output_dim=8):
    model = tf.keras.Sequential([
        tf.keras.layers.Input(shape=(input_dim,)),
        tf.keras.layers.Dense(128, activation='relu'),
        tf.keras.layers.Dense(128, activation='relu'),
        tf.keras.layers.Dense(64, activation='relu'),
        tf.keras.layers.Dense(output_dim, activation='softmax')
    ])
    model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])
    return model

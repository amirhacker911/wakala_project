#!/usr/bin/env python3
"""Scheduled trainer job: collects labeled samples, trains TF model, converts to TFLite and writes model_meta.json"""
import os, json, time
from server.db import get_all_samples
from server.ai_service.model_tf_training import load_dataset, build_model
from server.ai_service.convert_to_tflite import convert_to_tflite
import numpy as np

OUT_DIR = os.path.join(os.path.dirname(__file__), '..', 'ai_service', 'tflite_models')
os.makedirs(OUT_DIR, exist_ok=True)

def run_training_cycle():
    X, y = load_dataset()
    if X is None or len(X) < 20:
        print('Not enough labeled data to train. Need >=20, have', 0 if X is None else len(X))
        return False
    # train TF model using model_tf_training
    model = build_model(input_dim=X.shape[1], output_dim=len(set(y)))
    model.fit(X, y, epochs=12, batch_size=16, verbose=1)
    h5 = os.path.join(OUT_DIR, 'model.h5')
    model.save(h5)
    ok, tflite_path = convert_to_tflite()
    # write meta
    meta = {'saved_at': time.time(), 'n_samples': int(X.shape[0]), 'tflite': os.path.basename(tflite_path) if ok else None}
    with open(os.path.join(OUT_DIR,'model_meta.json'),'w',encoding='utf-8') as fh:
        json.dump(meta, fh)
    print('Training cycle complete. meta:', meta)
    return True

if __name__ == '__main__':
    run_training_cycle()

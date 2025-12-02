from fastapi import APIRouter, HTTPException, Request
from pydantic import BaseModel
import os, json
from server.db import add_sample, get_all_samples
from server.ai.hybrid_game_predictor import HybridGamePredictorV2 as HybridGamePredictor

router = APIRouter(prefix='/game', tags=['game'])
predictor = HybridGamePredictor()

class GameSampleIn(BaseModel):
    features: dict
    raw_text: str = ""
    metadata: dict = {}

@router.post('/upload')
def upload_sample(sample: GameSampleIn):
    if not sample.features:
        raise HTTPException(status_code=400, detail='no features')
    sid = add_sample(json.dumps(sample.features), None, sample.metadata)
    return {'status':'ok','id': sid}

@router.post('/predict')
def get_prediction(body: dict):
    try:
        res = predictor.predict(body)
        return res
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post('/train')
def train_model():
    # simple trigger for training job; in production this should be scheduled
    try:
        from server.jobs.trainer_job import run_training_cycle
        ok = run_training_cycle()
        return {'status':'started' if ok else 'no_data'}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

from fastapi import APIRouter, HTTPException
import os, json
router = APIRouter(prefix='/admin_stats', tags=['admin_stats'])

@router.get('/time_signature')
def time_signature():
    base = os.path.join(os.path.dirname(__file__), '..', '..', 'ai_service', 'tflite_models')
    p = os.path.join(base, 'time_signature.json')
    if os.path.exists(p):
        with open(p,'r',encoding='utf-8') as fh:
            return json.load(fh)
    return {'minute_heat': [0]*60, 'note':'no data'}

@router.get('/model_stats')
def model_stats():
    base = os.path.join(os.path.dirname(__file__), '..', '..', 'ai_service', 'tflite_models')
    p = os.path.join(base, 'model_meta.json')
    if os.path.exists(p):
        with open(p,'r',encoding='utf-8') as fh:
            return json.load(fh)
    return {}

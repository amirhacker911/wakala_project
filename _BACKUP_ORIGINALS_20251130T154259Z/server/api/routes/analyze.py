from fastapi import APIRouter
from pydantic import BaseModel
from server.ai_service import ai_main
router = APIRouter(prefix='', tags=['analyze'])

class PredictRequest(BaseModel):
    features: dict = {}
    options: list = []

@router.post('/getPrediction')
def get_prediction(req: PredictRequest):
    data = {'features': req.features, 'options': req.options}
    res = ai_main.predict(data)
    return res

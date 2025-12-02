from fastapi import APIRouter, HTTPException, Request
from pydantic import BaseModel
from server.db import init_db, get_all_samples
import os, json
from sqlmodel import Session, select
from server.db import engine, Sample

router = APIRouter(prefix='/labels', tags=['labels'])

class LabelIn(BaseModel):
    sample_id: int
    label: str

@router.post('/update')
def update_label(data: LabelIn, request: Request):
    api_key = request.headers.get('X-API-KEY') or request.query_params.get('api_key') or os.environ.get('API_KEY')
    if not api_key or api_key != os.environ.get('API_KEY'):
        raise HTTPException(status_code=401, detail='unauthorized')
    init_db()
    with Session(engine) as sess:
        s = sess.get(Sample, data.sample_id)
        if not s:
            raise HTTPException(status_code=404, detail='sample not found')
        s.label = data.label
        sess.add(s); sess.commit()
    return {'status':'updated','sample_id': data.sample_id, 'label': data.label}

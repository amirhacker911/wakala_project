from fastapi import APIRouter, Request, HTTPException
from fastapi.templating import Jinja2Templates
from fastapi.responses import HTMLResponse, RedirectResponse, JSONResponse, FileResponse
import os, json, time
from server.db import init_db, get_all_samples, get_user_by_id, approve_user

router = APIRouter(prefix='/admin', tags=['admin'])
templates = Jinja2Templates(directory=os.path.join(os.path.dirname(__file__), '..', '..', 'templates'))

@router.get('/', response_class=HTMLResponse)
def admin_index(request: Request):
    init_db()
    samples = get_all_samples()
    return templates.TemplateResponse('admin_index.html', {'request': request, 'samples': samples})

@router.get('/model_info')
def model_info():
    d = {}
    base = os.path.join(os.path.dirname(__file__), '..', '..', 'ai_service', 'tflite_models')
    meta = os.path.join(base, 'model_meta.json')
    if os.path.exists(meta):
        with open(meta,'r',encoding='utf-8') as fh:
            try:
                d = json.load(fh)
            except:
                d = {}
    return JSONResponse(d)

@router.get('/download_model')
def download_model():
    base = os.path.join(os.path.dirname(__file__), '..', '..', 'ai_service', 'tflite_models')
    f = os.path.join(base, 'model.tflite')
    if os.path.exists(f):
        return FileResponse(f, media_type='application/octet-stream', filename='model.tflite')
    raise HTTPException(status_code=404, detail='model not found')

@router.post('/approve_user/{user_id}')
def approve(request: Request, user_id: int):
    api_key = request.headers.get('X-API-KEY') or os.environ.get('API_KEY')
    if not api_key or api_key != os.environ.get('API_KEY'):
        raise HTTPException(status_code=401, detail='unauthorized')
    ok = approve_user(user_id)
    if not ok:
        raise HTTPException(status_code=404, detail='user not found')
    return RedirectResponse(url='/admin', status_code=303)

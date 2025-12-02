from fastapi import APIRouter, Depends, Header, HTTPException, Request
from server.db import get_all_samples, init_db, get_user_by_id
from server.ai_service import trainer, features as featmod
import json, os

router = APIRouter(prefix='', tags=['train'])

def _get_user_from_auth(request: Request):
    auth = request.headers.get('Authorization')
    if not auth or not auth.lower().startswith('bearer '):
        raise HTTPException(status_code=401, detail='Missing auth token')
    token = auth.split(' ',1)[1]
    secret = os.environ.get('JWT_SECRET','dev_jwt_secret_change')
    try:
        payload = jwt.decode(token, secret, algorithms=['HS256'])
        user_id = payload.get('sub')
        user = get_user_by_id(user_id)
        if not user:
            raise HTTPException(status_code=401, detail='invalid user')
        if not user.is_approved:
            raise HTTPException(status_code=403, detail='user not approved yet')
        return user
    except Exception as e:
        raise HTTPException(status_code=401, detail='invalid token')

@router.post('/trainModel')
def train_model(request: Request):
    init_db()
    user = _get_user_from_auth(request)
    samples = get_all_samples()
    if not samples:
        return {'status':'no_data'}
    X = []
    y = []
    for s in samples:
        d = json.loads(s.features)
        vec = featmod.vectorize(d)
        X.append(vec)
        y.append(s.label if s.label is not None else '0')
    out_dir = os.path.join(os.path.dirname(__file__), '..', 'ai_service')
    os.makedirs(out_dir, exist_ok=True)
    out_path = os.path.join(out_dir, 'model.joblib')
    success = trainer.train_from_data(X, y, out_path)
    if success:
        return {'status':'trained','model': out_path, 'trained_by': user.username}
    else:
        return {'status':'failed'}

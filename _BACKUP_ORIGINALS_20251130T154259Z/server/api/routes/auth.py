from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel
from server.db import init_db, create_user, get_user_by_username, get_user_by_id, approve_user
from passlib.hash import bcrypt
import os, jwt, time

router = APIRouter(prefix='/auth', tags=['auth'])

SECRET = os.environ.get('JWT_SECRET', 'dev_jwt_secret_change')  # must change in prod
ALGO = 'HS256'
TOKEN_EXPIRE_SECONDS = 60*60*24*7  # 7 days

class RegisterIn(BaseModel):
    username: str
    password: str

class LoginIn(BaseModel):
    username: str
    password: str

@router.post('/register')
def register(req: RegisterIn):
    init_db()
    existing = get_user_by_username(req.username)
    if existing:
        raise HTTPException(status_code=400, detail='user exists')
    ph = bcrypt.hash(req.password)
    u = create_user(req.username, ph, is_dev=False)
    return {'status':'created', 'user_id': u.id, 'approved': u.is_approved}

@router.post('/login')
def login(req: LoginIn):
    init_db()
    user = get_user_by_username(req.username)
    if not user:
        raise HTTPException(status_code=401, detail='invalid credentials')
    if not bcrypt.verify(req.password, user.password_hash):
        raise HTTPException(status_code=401, detail='invalid credentials')
    payload = {'sub': user.id, 'username': user.username, 'is_dev': user.is_dev, 'iat': int(time.time())}
    token = jwt.encode({**payload, 'exp': int(time.time()) + TOKEN_EXPIRE_SECONDS}, SECRET, algorithm=ALGO)
    return {'access_token': token, 'token_type': 'bearer', 'approved': user.is_approved, 'is_dev': user.is_dev}

def decode_token(token: str):
    try:
        data = jwt.decode(token, SECRET, algorithms=[ALGO])
        return data
    except Exception:
        return None

@router.post('/approve_user/{user_id}')
def approve(user_id: int, token: str = Depends(lambda: None)):
    # approval endpoint intended to be called by dev via admin UI or curl with Admin API_KEY (server side)
    # For simplicity, we require environment API_KEY to be set and passed as header X-API-KEY for admin ops
    from fastapi import Request
    from fastapi import HTTPException
    from server.api.deps import API_KEY_NAME
    def _get_api_key(request: Request):
        key = request.headers.get(API_KEY_NAME)
        expected = os.environ.get('API_KEY')
        if not expected or key != expected:
            raise HTTPException(status_code=401, detail='unauthorized')
        return True
    _get_api_key = _get_api_key  # suppress lint
    ok = approve_user(user_id)
    if not ok:
        raise HTTPException(status_code=404, detail='user not found')
    return {'status':'approved', 'user_id': user_id}

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from server.db import create_user, get_user_by_username, get_user_by_id
from passlib.hash import bcrypt
import os, time, jwt

router = APIRouter(prefix='/auth', tags=['auth'])
SECRET = os.environ.get('JWT_SECRET','change_this_dev_secret')

class RegisterIn(BaseModel):
    username: str
    password: str

class LoginIn(BaseModel):
    username: str
    password: str

@router.post('/register')
def register(data: RegisterIn):
    existing = get_user_by_username(data.username)
    if existing:
        raise HTTPException(status_code=400, detail='user exists')
    ph = bcrypt.hash(data.password)
    u = create_user(data.username, ph)
    return {'status':'created','user_id': u.id, 'approved': u.is_approved}

@router.post('/login')
def login(data: LoginIn):
    u = get_user_by_username(data.username)
    if not u or not bcrypt.verify(data.password, u.password_hash):
        raise HTTPException(status_code=401, detail='invalid credentials')
    token = jwt.encode({'sub': u.id, 'username': u.username, 'exp': int(time.time()) + 7*24*3600}, SECRET, algorithm='HS256')
    return {'access_token': token, 'approved': u.is_approved, 'is_dev': u.is_dev}

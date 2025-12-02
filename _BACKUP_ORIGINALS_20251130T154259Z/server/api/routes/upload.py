from fastapi import APIRouter, Depends, Header, HTTPException, Request
from pydantic import BaseModel
from server.db import add_sample, init_db, get_user_by_id
from server.api.deps import API_KEY_NAME
import json
from typing import Optional
import jwt, os

router = APIRouter(prefix='', tags=['upload'])

class SampleIn(BaseModel):
    features: dict
    raw_text: str = ''
    label: Optional[str] = None

def _get_user_from_auth(request: Request):
    # Expect header Authorization: Bearer <token>
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
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail='token expired')
    except Exception as e:
        raise HTTPException(status_code=401, detail='invalid token')

@router.post('/uploadSample')
def upload_sample(sample: SampleIn, request: Request):
    init_db()
    user = _get_user_from_auth(request)
    sid = add_sample(json.dumps(sample.features), sample.label)
    return {'status':'ok','id': sid, 'user': user.username}

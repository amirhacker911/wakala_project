from fastapi import Header, HTTPException, Security
from fastapi.security.api_key import APIKeyHeader
import os

API_KEY_NAME = 'X-API-KEY'
api_key_header = APIKeyHeader(name=API_KEY_NAME, auto_error=False)

EXPECTED_API_KEY = os.environ.get('API_KEY')
if not EXPECTED_API_KEY:
    raise RuntimeError('Environment variable API_KEY is required for server startup. Set API_KEY before starting the server.')

def get_api_key(api_key_header_value: str = Security(api_key_header)):
    if not api_key_header_value or api_key_header_value != EXPECTED_API_KEY:
        raise HTTPException(status_code=401, detail='Invalid or missing API Key')
    return api_key_header_value

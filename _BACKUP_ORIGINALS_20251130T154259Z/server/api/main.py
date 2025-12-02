from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from api.routes import health, analyze, upload, train, auth
import os

app = FastAPI(title='Wakala Fakhr AlArab API')

allowed = os.environ.get('ALLOWED_ORIGINS', 'http://localhost:8000,http://127.0.0.1:8000').split(',')
app.add_middleware(
    CORSMiddleware,
    allow_origins=allowed,
    allow_credentials=True,
    allow_methods=['*'],
    allow_headers=['*'],
)

app.include_router(health.router)
app.include_router(auth.router)
app.include_router(analyze.router)
app.include_router(upload.router)
app.include_router(train.router)

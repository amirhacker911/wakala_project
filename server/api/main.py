from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import os
from .routes.predict import router as predict_router
from .routes.game_control import router as game_control_router

app = FastAPI(title='WakalaFakhrAlArab API')

app.add_middleware(
    CORSMiddleware,
    allow_origins=os.environ.get('ALLOWED_ORIGINS','*').split(','),
    allow_credentials=True,
    allow_methods=['*'],
    allow_headers=['*']
)

app.include_router(predict_router, prefix='/api')
app.include_router(game_control_router, prefix='/api')

@app.get('/health')
async def health():
    return {'status': 'ok'}

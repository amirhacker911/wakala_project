from fastapi import APIRouter, Request, HTTPException
from fastapi.responses import JSONResponse
from ..utils.load_model import ModelWrapper
from server.ai import game_engine


router = APIRouter()
model_wrapper = ModelWrapper()

@router.post('/predict')
async def predict(request: Request):
    # try form file first
    try:
        form = await request.form()
        if 'file' in form:
            file = form['file']
            content = await file.read()
            result = model_wrapper.predict_from_bytes(content)
            return JSONResponse({'success': True, 'result': result})
    except Exception:
        pass

    # otherwise JSON body
    try:
        data = await request.json()
    except Exception:
        raise HTTPException(status_code=400, detail='Invalid JSON or no file uploaded')

    
    # Unified processing: integrate multipliers/time into input
    features = data.get('features', data.get('input', {}))
    multipliers = data.get('multipliers') or features.get('multipliers')
    game_time = data.get('time') or features.get('time')
    if multipliers:
        features['multipliers'] = multipliers
    if game_time:
        features['time'] = game_time
    data['input'] = features
    if 'input' not in data:
        raise HTTPException(status_code=400, detail="Provide 'input' field matching model input format")

    # If caller asked for game engine, use it (this is our hybrid AI+game logic)
    try:
        inp = data.get('input', {})
        use_game = data.get('game_mode') or inp.get('game_mode') or data.get('use_game_engine')
        if use_game:
            result = game_engine.predict(inp)
        else:
            result = model_wrapper.predict(inp)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f'Prediction error: {e}')

    return JSONResponse({'success': True, 'result': result})


@router.post('/test_predict')
async def test_predict(request: Request):
    try:
        data = await request.json()
    except Exception:
        raise HTTPException(status_code=400, detail='Invalid JSON')
    inp = data.get('input') or data
    # force game engine when requested
    use_game = data.get('game_mode') or data.get('use_game_engine') or (isinstance(inp, dict) and inp.get('use_game_engine'))
    try:
        if use_game:
            from server.ai import game_engine
            res = game_engine.predict(inp)
        else:
            res = model_wrapper.predict(inp)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    return JSONResponse({'success': True, 'result': res})


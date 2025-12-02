
from fastapi import APIRouter, HTTPException, Request
from fastapi.responses import JSONResponse
from server.config import game_config
from server.ai import game_engine
import statistics, json

router = APIRouter(prefix='', tags=['game_control'])

@router.get('/game/settings')
def get_settings():
    s = game_config.get_settings()
    return JSONResponse({'success': True, 'settings': s})

@router.post('/game/settings')
async def post_settings(req: Request):
    data = await req.json()
    # validate multipliers if present
    if 'multipliers' in data:
        m = data['multipliers']
        if not isinstance(m, list) or len(m) < 2:
            raise HTTPException(status_code=400, detail='multipliers must be a list with at least 2 values')
    saved = game_config.save(data)
    return JSONResponse({'success': True, 'settings': saved})

@router.post('/game/simulate')
async def simulate(req: Request):
    data = await req.json()
    rounds = int(data.get('rounds', 50))
    sample_input = data.get('input', {})
    settings = game_config.get_settings()
    # merge multipliers and time into sample_input
    sample_input = dict(sample_input)
    sample_input.setdefault('multipliers', settings.get('multipliers'))
    sample_input.setdefault('options', [str(i) for i in range(len(sample_input.get('multipliers')))])
    results = []
    for r in range(rounds):
        inp = dict(sample_input)
        inp['round'] = r+1
        # optionally vary time if provided as pattern
        if 'hour' not in inp or inp.get('hour') is None:
            inp['hour'] = settings.get('win_hour')
        if 'minute' not in inp or inp.get('minute') is None:
            inp['minute'] = settings.get('win_minute')
        res = game_engine.predict(inp)
        results.append(res)
    # compute summary stats
    winners = [r['winner'] for r in results]
    freq = {k: winners.count(k) for k in set(winners)}
    avg_conf = statistics.mean([r.get('confidence',0) for r in results]) if results else 0
    return JSONResponse({'success': True, 'rounds': rounds, 'freq': freq, 'avg_confidence': avg_conf, 'sample': results[:10]})

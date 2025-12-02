
import os, json
from pathlib import Path

DEFAULT = {
    "multipliers": [3,5,8,12,15,20,50,100],
    "win_hour": None,
    "win_minute": None
}

CONFIG_FILE = Path(__file__).resolve().parent / "game_settings.json"

def _ensure():
    if not CONFIG_FILE.exists():
        save(DEFAULT)
    try:
        with open(CONFIG_FILE,'r',encoding='utf-8') as f:
            data = json.load(f)
    except Exception:
        data = DEFAULT
        save(data)
    return data

def get_settings():
    return _ensure()

def save(settings: dict):
    s = DEFAULT.copy()
    s.update(settings or {})
    with open(CONFIG_FILE,'w',encoding='utf-8') as f:
        json.dump(s, f, indent=2)
    return s

# time_pattern_engine.py
from typing import Optional
HOT_MINUTES = {12,25,35,43,48}

def time_boost(minute: Optional[int]) -> float:
    """Return a small boost factor for minutes known to favor certain outcomes (e.g., 100x)."""
    if minute is None:
        return 0.0
    return 1.0 if minute in HOT_MINUTES else 0.0

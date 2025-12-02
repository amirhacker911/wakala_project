# frequency_model.py
from collections import Counter
from typing import List, Dict

def frequency_probabilities(history: List[int]) -> Dict[int, float]:
    if not history: return {}
    c = Counter(history)
    total = sum(c.values()) or 1
    return {k: c[k]/total for k in c}

# hybrid_predictor.py
from typing import List, Dict, Optional
from .sequence_detector import detect_sequence_pattern
from .markov_model import markov_next_probabilities
from .frequency_model import frequency_probabilities
from .time_pattern_engine import time_boost

# fixed slot -> multiplier mapping (indexes 0..7)
SLOT_MULTIPLIER = {0:3,1:3,2:8,3:8,4:20,5:20,6:100,7:100}

def hybrid_predict(history: List[int], minute: Optional[int]=None) -> Dict:
    """Return prediction dict: {prediction:int, confidence:float, reason:str, scores:dict} """
    # canonicalize history as integers
    hist = list(map(int, history)) if history else []
    scores = {k:0.0 for k in SLOT_MULTIPLIER.keys()}

    # 1) sequence detect
    seq = detect_sequence_pattern(hist, max_size=6)
    if seq:
        # assume next is first element of seq repeating
        next_val = seq[0]
        # if next_val corresponds to some slot(s), increase score for those slots
        for slot,m in SLOT_MULTIPLIER.items():
            if m == next_val:
                scores[slot] += 3.0

    # 2) markov probabilities based on last value
    markov_probs = markov_next_probabilities(hist)
    for val, p in markov_probs.items():
        for slot,m in SLOT_MULTIPLIER.items():
            if m == val:
                scores[slot] += p * 2.0

    # 3) frequency probabilities
    freq = frequency_probabilities(hist)
    for val,p in freq.items():
        for slot,m in SLOT_MULTIPLIER.items():
            if m == val:
                scores[slot] += p * 1.0

    # 4) time boost for 100x preference
    tb = time_boost(minute)
    if tb>0:
        # boost 100x slots
        for slot,m in SLOT_MULTIPLIER.items():
            if m == 100:
                scores[slot] += tb * 2.0

    # normalize into confidence between 0..1
    total = sum(scores.values()) or 1.0
    # pick best slot index
    best_slot = max(scores, key=lambda k:scores[k])
    confidence = scores[best_slot] / total if total>0 else 0.0
    reason_parts = []
    if seq: reason_parts.append('pattern')
    if markov_probs: reason_parts.append('markov')
    if freq: reason_parts.append('freq')
    if tb>0: reason_parts.append('time')
    reason = '+'.join(reason_parts) if reason_parts else 'baseline'
    return {'prediction_slot': best_slot, 'prediction_multiplier': SLOT_MULTIPLIER[best_slot], 'confidence': round(confidence,3), 'reason': reason, 'scores': scores}

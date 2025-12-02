# sequence_detector.py
from typing import List, Optional

def detect_sequence_pattern(history: List[int], max_size: int = 6) -> Optional[List[int]]:
    """Detect simple repeated tail sequence. Returns the repeating block if found."""
    n = len(history)
    for size in range(2, min(max_size, n//2) + 1):
        if history[-size:] == history[-2*size:-size]:
            return history[-size:]
    return None

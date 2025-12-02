
import hashlib, math, json, os

VECTOR_LEN = 64

def _hash_token(token: str):
    h = hashlib.sha256(str(token).encode('utf-8')).hexdigest()
    return int(h[:8], 16)

def safe_float(x):
    try:
        return float(x)
    except Exception:
        try:
            return float(str(x).replace(',', '.'))
        except Exception:
            return 0.0

def vectorize(data: dict):
    """
    Minimal, robust vectorizer used by training code.
    Accepts dict with keys: multipliers (list), numbers (list), tokens (list)
    Returns fixed-length vector of length VECTOR_LEN.
    """
    vec = [0.0] * VECTOR_LEN
    if not isinstance(data, dict):
        return vec
    # incorporate multipliers
    muls = data.get('multipliers') or []
    max_mul = max([safe_float(m) for m in muls] + [1.0])
    for i, m in enumerate(muls[:16]):  # use up to 16 multiplier features
        idx = (i + _hash_token(f"mul:{m}")) % VECTOR_LEN
        vec[idx] += safe_float(m) / max_mul
    # incorporate numeric features
    nums = data.get('numbers') or []
    for j, n in enumerate(nums[:10]):
        idx = (4 + j) % VECTOR_LEN
        vec[idx] = safe_float(n)
    # incorporate tokens as hashed positions
    toks = data.get('tokens') or []
    for j, t in enumerate(toks[:10]):
        idx = _hash_token(t) % VECTOR_LEN
        vec[idx] += 1.0
    # normalize
    maxv = max(abs(x) for x in vec) or 1.0
    vec = [x / maxv for x in vec]
    return vec

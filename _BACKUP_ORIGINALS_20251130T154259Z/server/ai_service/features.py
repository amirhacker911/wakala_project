import hashlib

VECTOR_LEN = 64

def _hash_token(token: str):
    h = hashlib.sha256(token.encode('utf-8')).hexdigest()
    return int(h[:8], 16)

def vectorize(data):
    vec = [0.0] * VECTOR_LEN
    if not isinstance(data, dict):
        return vec
    idx = 0
    # numeric features first
    for k,v in data.items():
        if isinstance(v, (int,float)) and idx < VECTOR_LEN//4:
            vec[idx] = float(v)
            idx += 1
    # textual features hashed into buckets
    for k,v in data.items():
        if isinstance(v, str):
            tokens = v.lower().split()
            for t in tokens:
                b = _hash_token(k+':'+t) % VECTOR_LEN
                vec[b] += 1.0
    # lists and dicts
    for k,v in data.items():
        if isinstance(v, dict):
            for kk,vv in v.items():
                try:
                    f = float(vv)
                    vec[abs(hash(kk)) % VECTOR_LEN] += f
                except:
return None  # AUTO_REPLACED_PASS
        if isinstance(v, list):
            for it in v:
                if isinstance(it, (int,float)):
                    vec[abs(hash(k)) % VECTOR_LEN] += float(it)
                elif isinstance(it, str):
                    b = _hash_token(k+':'+it) % VECTOR_LEN
                    vec[b] += 1.0
    # normalize
    maxv = max(abs(x) for x in vec) or 1.0
    vec = [x/maxv for x in vec]
    return vec

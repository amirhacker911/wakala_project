
import math, random, hashlib
from datetime import datetime

DEFAULT_MULTIPLIERS = [3, 5, 8, 12, 15, 20, 50, 100]

# Configurable winning time (can be changed by admin)
WIN_HOUR = None   # e.g., 15 for 3pm -- None disables hard 100x time event
WIN_MINUTE = None # e.g., 30 for :30

def _seed_from_context(seed_values):
    # produce deterministic seed from context values
    s = "|".join(str(x) for x in seed_values)
    h = hashlib.sha256(s.encode()).hexdigest()
    return int(h[:16], 16)

def _normalize(scores):
    s = sum(scores) or 1.0
    return [float(x)/s for x in scores]

def predict(game_input):
    """
    game_input expected dictionary fields (recommended):
      - amount: numeric bet
      - choice: user's chosen option label/index (optional, not required for prediction output)
      - round: integer round number (optional)
      - hour, minute: integers for time context (optional)
      - history: list of previous winning option labels/indices (most recent last) (optional)
      - multipliers: list of multipliers for each option (optional)
      - options: list of option ids/names (optional, default 8 options)
    Returns dict:
      - winner: chosen winning option
      - multiplier: multiplier for the winner
      - final_amount: amount * multiplier (if amount provided)
      - confidence: float 0..1 estimated confidence
      - reason: explanation combining time/repeat/ai
      - scores: normalized scores for all options
    """
    # defaults and inputs
    amount = game_input.get("amount", None)
    choice = game_input.get("choice", None)
    round_no = int(game_input.get("round", 0) or 0)
    hour = game_input.get("hour", None)
    minute = game_input.get("minute", None)
    history = list(game_input.get("history") or [])
    multipliers = list(game_input.get("multipliers") or DEFAULT_MULTIPLIERS)
    options = list(game_input.get("options") or [str(i) for i in range(len(multipliers))])

    n = max(len(options), len(multipliers))
    # normalize lengths if needed
    if len(multipliers) < n:
        multipliers = (multipliers + DEFAULT_MULTIPLIERS)[:n]
    if len(options) < n:
        options = options + [str(i) for i in range(len(options), n)]

    # base uniform scores
    base_scores = [1.0 for _ in range(n)]

    # 1) History / repetition boost
    # Count occurrences in last 20 rounds (more weight to recent)
    recent = history[-20:]
    weights = {}
    total_weight = 0.0
    for idx, val in enumerate(recent[::-1]):  # recent[::-1] makes most recent first
        # more recent gets higher weight: 1/(1+pos)
        pos = idx
        w = 1.0 / (1 + pos)
        weights[val] = weights.get(val, 0.0) + w
        total_weight += w
    # apply to base_scores by mapping option labels to indices
    label_to_index = {str(options[i]): i for i in range(n)}
    for label, w in weights.items():
        if str(label) in label_to_index:
            i = label_to_index[str(label)]
            # amplify base score modestly based on repetition weight
            base_scores[i] *= (1.0 + 0.6 * (w / (total_weight or 1.0)))

    # 2) Time signature boost (special 100x event)
    time_reason = None
    if hour is not None and minute is not None and WIN_HOUR is not None and WIN_MINUTE is not None:
        try:
            ih = int(hour); im = int(minute)
            if ih == WIN_HOUR and im == WIN_MINUTE:
                # deterministic pick: combine round, hour, minute into index
                idx = (_seed_from_context([round_no, ih, im]) % n)
                # give this option a very large boost so it becomes the winner with 100x
                base_scores[idx] *= 99999.0
                time_reason = f"time_match ({WIN_HOUR:02d}:{WIN_MINUTE:02d}) -> forced index {idx}"
        except Exception:
            pass

    # 3) Multiplier sensitivity: reward options with large multipliers slightly in analysis
    for i, m in enumerate(multipliers):
        try:
            mv = float(m)
        except Exception:
            mv = 1.0
        # scale: prefer mid-to-high multipliers but cap greedy preference
        base_scores[i] *= (1.0 + min(0.8, math.log1p(mv) / 5.0))

    # 4) Seeded pseudo-randomness to break ties but be reproducible
    seed_values = [round_no, hour or 0, minute or 0, len(recent)]
    seed = _seed_from_context(seed_values)
    rnd = random.Random(seed)

    # apply small random jitter reproducibly
    for i in range(n):
        base_scores[i] *= (1.0 + (rnd.random() - 0.5) * 0.06)  # +/-3% jitter

    # normalize to probabilities
    probs = _normalize(base_scores)

    # confidence measure: higher if one option dominates and multiplier large
    max_prob = max(probs)
    winner_idx = int(max(range(n), key=lambda i: probs[i]))
    winner = options[winner_idx]
    multiplier = float(multipliers[winner_idx])
    confidence = max_prob  # baseline
    # boost confidence when multiplier is extreme and recent repetition supports it
    confidence = min(1.0, confidence + min(0.25, math.log1p(multiplier)/10.0))

    # assemble reason parts
    reasons = []
    if time_reason:
        reasons.append(time_reason)
    if recent:
        reasons.append(f"history_bias={dict(weights)}")
    reasons.append("hybrid_prob_model")

    final_amount = None
    try:
        if amount is not None:
            final_amount = float(amount) * multiplier
    except Exception:
        final_amount = None

    return {
        "winner": winner,
        "winner_index": winner_idx,
        "multiplier": multiplier,
        "final_amount": final_amount,
        "confidence": round(float(confidence), 4),
        "reason": "; ".join(reasons),
        "scores": {options[i]: round(float(probs[i]),6) for i in range(n)}
    }

# markov_model.py
from collections import Counter, defaultdict
from typing import List, Dict

def build_markov(history: List[int]) -> Dict[int, Counter]:
    transitions = defaultdict(list)
    for i in range(len(history)-1):
        a, b = history[i], history[i+1]
        transitions[a].append(b)
    return {k: Counter(v) for k, v in transitions.items()}

def markov_next_probabilities(history: List[int]):
    if not history: return {}
    markov = build_markov(history)
    last = history[-1]
    cnt = markov.get(last, Counter())
    total = sum(cnt.values()) or 1
    return {k: v/total for k, v in cnt.items()}

#!/usr/bin/env python3
import time, os, json
from server.db import get_unlabeled_samples, get_all_samples
from server.jobs.trainer_job import run_training_cycle

THRESHOLD = int(os.environ.get('RETRAIN_THRESHOLD', '200'))

if __name__ == '__main__':
    unl = get_unlabeled_samples()
    print('unlabeled samples:', len(unl))
    if len(unl) >= THRESHOLD:
        print('Threshold reached - running training cycle')
        run_training_cycle()
    else:
        print('Not enough unlabeled to retrain')

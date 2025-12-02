#!/bin/bash
set -e
echo "Activating venv (if exists)..."
if [ -f venv/bin/activate ]; then
  source venv/bin/activate
fi
echo "Installing training requirements if provided..."
if [ -f training/requirements.txt ]; then
  pip install -r training/requirements.txt
fi
echo "Starting training script..."
python training/train.py
echo "Training finished. Check training output directory for model files."

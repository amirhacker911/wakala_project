# Colab / Local Training Instructions

Steps to create a TFLite model from dataset:

1. Generate synthetic dataset (optional for testing):
   ```bash
   python training/generate_synthetic.py --out synthetic_dataset --per 500
   ```
2. Train model (on Colab or local machine with TF installed):
   ```bash
   python training/train.py --data synthetic_dataset --manifest manifest.json --epochs 20 --out model.h5
   ```
3. Convert to TFLite:
   ```bash
   python training/convert_to_tflite.py --h5 model.h5 --out model.tflite
   ```
4. Upload `model.tflite` to server via `/model/upload` or place into `server/uploads/models/` and update `latest.json`.
5. On device, ensure `BASE_URL` is set to your server and trigger the ModelUpdater or wait for periodic worker to download model.

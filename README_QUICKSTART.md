# QUICKSTART — WakalaFakhrAlArab (prepared)
## Local dev (no Docker)
1. From project root:
```
bash start_server.sh
```
This creates/uses `server/venv` and starts uvicorn on port 8000.
## With Docker Compose
```
docker compose up --build
```
## Training (on your machine)
```
bash training/run_training.sh
# then convert:
python training/convert_to_tflite_wrapper.py --input path/to/model.h5 --output server/models/model.tflite
```
## Place model for serving
Put your trained model into `server/models/` as either `model.h5` or `model.tflite`.
API will auto-load the first model it finds.

FastAPI backend for WakalaFakhrAlArab
- Uses SQLModel (supports SQLite for dev, Postgres in production via DATABASE_URL env var)
- JWT auth, bcrypt passwords, consent management
- Endpoints: /register, /token (OAuth2), /consent, /me, /samples/upload, /datasets/upload, /model/upload, /trigger-train, /models/latest.json
- To run (development): `uvicorn app_fastapi:app --reload --host 0.0.0.0 --port 8000`
- In production use Dockerfile and configure DATABASE_URL & SECRET_KEY in environment.

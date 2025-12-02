
#!/bin/bash
# Start FastAPI for local development
export PORT=${PORT:-8000}
uvicorn server.app_fastapi:app --host 0.0.0.0 --port ${PORT}

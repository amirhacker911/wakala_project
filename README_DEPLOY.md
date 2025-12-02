Wakala Fakhr Al Arab - Ready for Deploy (Generated files)

This archive contains deployment-ready helper files and client snippets to connect the mobile app to the API.
Steps to deploy (Vultr VPS recommended):
1. Place the provided docker-compose.yml at the root of your project.
2. Put server/.env in the server/ folder and update values (SECRET_KEY, API_KEY, DATABASE_URL if using external DB).
3. Upload the project to your server (git clone or scp), then run:
   sudo apt update && sudo apt install -y docker.io docker-compose
   docker compose up -d --build
4. Update app/src/config.js -> API_BASE_URL to your public API URL (https://api.example.com).
5. Build the Android app (if native) or run using Expo depending on your app stack.

Notes:
- The AI service is expected to run inside server/ai_service and listen on port 9000 (AI_SERVICE_URL).
- If you prefer to use Neon/Supabase free DB, set DATABASE_URL accordingly.
- For production, change SECRET_KEY and secure your env values.


# FastAPI deployment note
Run the app with uvicorn:

```
uvicorn server.app_fastapi:app --host 0.0.0.0 --port $PORT
```

Ensure your trained model file(s) are placed in `server/models/` (e.g. `server/models/dummy_model.joblib`).

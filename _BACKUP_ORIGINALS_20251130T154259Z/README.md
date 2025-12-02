

# UI Overhaul
App uses a black+gold theme, Material components, adaptive launcher icon, and polished layouts in res/layout.


# Authentication
- Register via POST /auth/register
- Login via POST /auth/login to receive access_token
- Developer/admin approves users via POST /auth/approve_user/{user_id} with X-API-KEY header
- App stores token in SecureStore and sends Authorization: Bearer <token> for protected endpoints


---
Assistant: Added PRO hybrid predictor (pattern+markov+freq+time) and client helpers. Fixed fixed-slot multiplier mapping (slots 0..7 -> 3,3,8,8,20,20,100,100).


Assistant: Server endpoints (/analyze-screenshot, /collect-training, /trigger-retrain, /download-model) added. Install tesseract-ocr on server for pytesseract support.

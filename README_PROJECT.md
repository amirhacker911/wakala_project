WakalaFakhrAlArab - Final Project (Autonomous pipeline)

Structure:
- app/ (Android project)
- server/ (Flask server to receive datasets and models)
- training/ (scripts to generate dataset, train and convert to TFLite)

Quick start:
1. Start server (locally or remote) and note its IP:PORT
2. Edit app/src/main/java/com/wakala/fakhr/Config.kt BASE_URL to your server URL (e.g. http://192.168.1.100:5000/)
3. Build and install APK on device (Android Studio) or request APK build.
4. Grant overlay and screen-capture permissions, request a screen capture once, start overlay.
5. Press floating button to capture and analyze — dataset will be collected in app filesDir/dataset/
6. Use DataLabelActivity to label samples, then use MainActivity -> Zip & Upload Dataset to send to server.
7. Server `/trigger-train` will be called by worker; it will produce a model and update latest.json. ModelUpdater downloads it.
8. Model will be available in filesDir/model.tflite and TFLiteModelManager will use it for predictions.

Training locally (optional):
- Use training/generate_synthetic.py and training/train.py to create a model.h5 and convert to TFLite using training/convert_to_tflite.py


Legal & Branding:
All rights reserved to وكالة فخر العرب. The application UI uses black and gold styling with the application name 'وكالة فخر العرب'. Users must register and explicitly consent before data collection occurs.


Pro version features added: Admin API, subscription endpoints, license activation, admin dashboard scaffold at /deploy/admin/index.html


AutoRoundWatcher feature:
- Service AutoRoundWatcher monitors last parsed timer seconds stored by OCRProcessor in SharedPreferences under key 'last_timer_seconds'.
- When it detects a new round (timer jumped from <=5 to >=25) it broadcasts ACTION_AUTO_ANALYZE to trigger AutoAnalyzerService.
- Ensure OCRProcessor calls saveLastTimerSeconds(context, seconds) after parsing the on-screen timer.
- AutoRoundWatcher starts on app launch and on boot if BootReceiver starts services.

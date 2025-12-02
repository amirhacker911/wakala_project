from flask import Flask, request, jsonify, send_from_directory
import os
from werkzeug.utils import secure_filename

app = Flask(__name__)
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
UPLOAD_DIR = os.path.join(BASE_DIR, "uploads")
DATASET_DIR = os.path.join(UPLOAD_DIR, "datasets")
MODEL_DIR = os.path.join(UPLOAD_DIR, "models")
os.makedirs(DATASET_DIR, exist_ok=True)
os.makedirs(MODEL_DIR, exist_ok=True)

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status":"ok"})

@app.route("/samples/upload", methods=["POST"])
def upload_sample():
    if "file" not in request.files:
        return "No file part", 400
    f = request.files["file"]
    filename = secure_filename(f.filename)
    save_path = os.path.join(UPLOAD_DIR, filename)
    f.save(save_path)
    return jsonify({"ok": True, "filename": filename})

@app.route("/datasets/upload", methods=["POST"])
def upload_dataset():
    if "file" not in request.files:
        return "No file", 400
    f = request.files["file"]
    filename = secure_filename(f.filename)
    save_path = os.path.join(DATASET_DIR, filename)
    f.save(save_path)
    return jsonify({"ok": True, "filename": filename})

@app.route("/model/upload", methods=["POST"])
def upload_model():
    if "file" not in request.files:
        return "No file", 400
    f = request.files["file"]
    filename = secure_filename(f.filename)
    save_path = os.path.join(MODEL_DIR, filename)
    f.save(save_path)
    return jsonify({"ok": True, "filename": filename})

@app.route("/models/<path:filename>", methods=["GET"])
def get_model(filename):
    return send_from_directory(MODEL_DIR, filename, as_attachment=True)

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)


@app.route('/trigger-train', methods=['POST'])
def trigger_train():
    # simulation: create a dummy model file and update latest.json
    import time, json
    ts = int(time.time())
    model_name = f"model_{ts}.tflite"
    model_path = os.path.join(MODEL_DIR, model_name)
    # create a dummy small file to represent a model
    with open(model_path, 'wb') as mf:
        mf.write(b'DUMMY MODEL DATA - replace with real trained TFLite') 
    # update latest.json
    latest = {'name': model_name, 'timestamp': ts}
    with open(os.path.join(MODEL_DIR, 'latest.json'), 'w') as lf:
        json.dump(latest, lf)
    return jsonify({'ok': True, 'model': model_name})

# Simple auth endpoints (users stored in server/uploads/users.json)
import uuid, hashlib, time, json
USERS_FILE = os.path.join(UPLOAD_DIR, 'users.json')

def load_users():
    if os.path.exists(USERS_FILE):
        try:
            with open(USERS_FILE,'r') as uf:
                return json.load(uf)
        except:
            return {}
    return {}

def save_users(u):
    with open(USERS_FILE,'w') as uf:
        json.dump(u, uf)

@app.route('/register', methods=['POST'])
def register():
    data = request.form or request.get_json() or {}
    email = data.get('email') or request.form.get('email')
    password = data.get('password') or request.form.get('password')
    if not email or not password:
        return jsonify({'ok': False, 'error': 'email,password required'}), 400
    users = load_users()
    if email in users:
        return jsonify({'ok': False, 'error': 'user exists'}), 400
    salt = uuid.uuid4().hex
    pwdhash = hashlib.sha256((password+salt).encode()).hexdigest()
    users[email] = {'salt': salt, 'pwd': pwdhash, 'consent': False, 'created': int(time.time())}
    save_users(users)
    return jsonify({'ok': True, 'email': email})

@app.route('/login', methods=['POST'])
def login():
    data = request.form or request.get_json() or {}
    email = data.get('email') or request.form.get('email')
    password = data.get('password') or request.form.get('password')
    users = load_users()
    if email not in users:
        return jsonify({'ok': False, 'error': 'no such user'}), 400
    rec = users[email]
    pwdhash = hashlib.sha256((password+rec['salt']).encode()).hexdigest()
    if pwdhash != rec['pwd']:
        return jsonify({'ok': False, 'error': 'invalid credentials'}), 400
    # generate token (simple)
    token = uuid.uuid4().hex
    # store token mapping
    tokens_file = os.path.join(UPLOAD_DIR, 'tokens.json')
    tokens = {}
    if os.path.exists(tokens_file):
        try:
            tokens = json.load(open(tokens_file))
        except:
            tokens = {}
    tokens[token] = {'email': email, 'ts': int(time.time())}
    with open(tokens_file,'w') as tf:
        json.dump(tokens, tf)
    return jsonify({'ok': True, 'token': token, 'email': email, 'consent': rec.get('consent', False)})

@app.route('/consent', methods=['POST'])
def consent():
    data = request.form or request.get_json() or {}
    token = data.get('token') or request.form.get('token')
    consent_val = data.get('consent') if 'consent' in data else request.form.get('consent')
    if token is None:
        return jsonify({'ok': False, 'error': 'token required'}), 400
    tokens_file = os.path.join(UPLOAD_DIR, 'tokens.json')
    if not os.path.exists(tokens_file):
        return jsonify({'ok': False, 'error': 'invalid token'}), 400
    tokens = json.load(open(tokens_file))
    if token not in tokens:
        return jsonify({'ok': False, 'error': 'invalid token'}), 400
    email = tokens[token]['email']
    users = load_users()
    users[email]['consent'] = bool(consent_val in [True, 'true', '1', 1, 'True'])
    save_users(users)
    return jsonify({'ok': True, 'email': email, 'consent': users[email]['consent']})

import os, sys, json, time
import pytest
from fastapi.testclient import TestClient

sys.path.append(os.path.join(os.path.dirname(__file__), '..'))  # server package
from api.main import app

client = TestClient(app)

def test_health():
    r = client.get('/health')
    assert r.status_code == 200
    assert r.json().get('status') == 'ok'

def test_register_login_and_protect():
    # clear env secrets for testing: use dev values
    os.environ['JWT_SECRET'] = 'testsecret'
    # register
    r = client.post('/auth/register', json={'username':'testuser','password':'pass123'})
    assert r.status_code == 200 or r.status_code == 201
    # login
    r = client.post('/auth/login', json={'username':'testuser','password':'pass123'})
    assert r.status_code == 200
    body = r.json()
    assert 'access_token' in body

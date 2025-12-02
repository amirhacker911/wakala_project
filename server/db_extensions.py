from sqlmodel import SQLModel, Field, create_engine, Session, select
import os, json, typing as t
from datetime import datetime

DB_URL = os.environ.get('DATABASE_URL','sqlite:///./wakala_final.db')
engine = create_engine(DB_URL, echo=False)

class GameHistory(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    round_id: t.Optional[str] = None
    timestamp: t.Optional[float] = None
    winner: t.Optional[str] = None
    features: t.Optional[str] = None

class ModelStat(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    version: str
    created_at: float
    n_samples: int
    metrics: t.Optional[str] = None

class TrainingJob(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    started_at: float
    finished_at: t.Optional[float] = None
    status: str
    details: t.Optional[str] = None

class UserDevice(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    user_id: int
    device_hash: str
    last_seen: float

def init_extensions():
    SQLModel.metadata.create_all(engine)

def add_game_history(round_id, ts, winner, features):
    init_extensions()
    with Session(engine) as sess:
        gh = GameHistory(round_id=round_id, timestamp=ts, winner=winner, features=json.dumps(features))
        sess.add(gh); sess.commit(); sess.refresh(gh)
        return gh.id

def get_recent_history(limit=500):
    init_extensions()
    with Session(engine) as sess:
        q = select(GameHistory).order_by(GameHistory.id.desc()).limit(limit)
        return list(sess.exec(q).all())

def add_model_stat(version, n_samples, metrics=None):
    init_extensions()
    with Session(engine) as sess:
        m = ModelStat(version=version, created_at=float(datetime.utcnow().timestamp()), n_samples=n_samples, metrics=json.dumps(metrics) if metrics else None)
        sess.add(m); sess.commit(); sess.refresh(m)
        return m.id

def add_training_job(started_at, status='running', details=None):
    init_extensions()
    with Session(engine) as sess:
        t = TrainingJob(started_at=started_at, status=status, details=details)
        sess.add(t); sess.commit(); sess.refresh(t)
        return t.id

def finish_training_job(job_id, status='done', details=None):
    init_extensions()
    with Session(engine) as sess:
        j = sess.get(TrainingJob, job_id)
        if not j:
            return False
        j.finished_at = float(datetime.utcnow().timestamp())
        j.status = status
        j.details = details
        sess.add(j); sess.commit()
        return True

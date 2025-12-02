from sqlmodel import SQLModel, Field, create_engine, Session, select
import os, json, typing as t

DB_URL = os.environ.get('DATABASE_URL','sqlite:///./wakala_final.db')
engine = create_engine(DB_URL, echo=False)

class User(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    username: str
    password_hash: str
    is_approved: bool = False
    is_dev: bool = False

class Sample(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    features: str
    label: t.Optional[str] = None
    metadata: t.Optional[str] = None

def init_db():
    SQLModel.metadata.create_all(engine)

def create_user(username: str, password_hash: str, is_dev: bool=False):
    init_db()
    with Session(engine) as sess:
        u = User(username=username, password_hash=password_hash, is_approved=False, is_dev=is_dev)
        sess.add(u); sess.commit(); sess.refresh(u)
        return u

def get_user_by_username(username: str):
    init_db()
    with Session(engine) as sess:
        q = select(User).where(User.username==username)
        return sess.exec(q).first()

def get_user_by_id(uid: int):
    init_db()
    with Session(engine) as sess:
        return sess.get(User, uid)

def approve_user(uid: int):
    init_db()
    with Session(engine) as sess:
        u = sess.get(User, uid)
        if not u:
            return False
        u.is_approved = True
        sess.add(u); sess.commit()
        return True

def add_sample(features_json: str, label: t.Optional[str]=None, metadata: t.Optional[dict]=None):
    init_db()
    with Session(engine) as sess:
        s = Sample(features=features_json, label=label, metadata=json.dumps(metadata) if metadata else None)
        sess.add(s); sess.commit(); sess.refresh(s)
        return s.id

def get_all_samples(limit: int=10000):
    init_db()
    with Session(engine) as sess:
        q = select(Sample).limit(limit)
        return list(sess.exec(q).all())

def get_unlabeled_samples():
    init_db()
    with Session(engine) as sess:
        q = select(Sample).where(Sample.label==None)
        return list(sess.exec(q).all())

def update_sample_label(sample_id: int, label: str):
    init_db()
    with Session(engine) as sess:
        s = sess.get(Sample, sample_id)
        if not s:
            return False
        s.label = label
        sess.add(s); sess.commit()
        return True

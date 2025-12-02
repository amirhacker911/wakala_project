from sqlmodel import SQLModel, Field, Session, create_engine, select
from typing import Optional, List
import os

DB_FILE = os.path.join(os.path.dirname(__file__), 'data.db')
engine = create_engine(f'sqlite:///{DB_FILE}', echo=False, connect_args={'check_same_thread': False})

class Sample(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    features: str  # JSON string
    label: Optional[str] = None

class User(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    username: str = Field(index=True, unique=True)
    password_hash: str
    is_approved: bool = Field(default=False)
    is_dev: bool = Field(default=False)

def init_db():
    SQLModel.metadata.create_all(engine)

def add_sample(features_json: str, label: str = None) -> int:
    with Session(engine) as sess:
        s = Sample(features=features_json, label=label)
        sess.add(s)
        sess.commit()
        sess.refresh(s)
        return s.id

def get_all_samples() -> List[Sample]:
    with Session(engine) as sess:
        return sess.exec(select(Sample)).all()

# User helpers
def create_user(username: str, password_hash: str, is_dev: bool = False):
    with Session(engine) as sess:
        u = User(username=username, password_hash=password_hash, is_approved=is_dev, is_dev=is_dev)
        sess.add(u)
        sess.commit()
        sess.refresh(u)
        return u

def get_user_by_username(username: str):
    with Session(engine) as sess:
        return sess.exec(select(User).where(User.username == username)).first()

def get_user_by_id(user_id: int):
    with Session(engine) as sess:
        return sess.exec(select(User).where(User.id == user_id)).first()

def approve_user(user_id: int):
    with Session(engine) as sess:
        u = sess.get(User, user_id)
        if not u:
            return False
        u.is_approved = True
        sess.add(u)
        sess.commit()
        return True

if __name__ == '__main__':
    init_db()
    print('DB initialized at', DB_FILE)

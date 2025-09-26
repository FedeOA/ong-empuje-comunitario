from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from .config import DB_USER, DB_PASS, DB_HOST, DB_PORT, DB_NAME
import logging

# Suppress SQLAlchemy logs
logging.getLogger('sqlalchemy.engine').setLevel(logging.WARNING)
logging.getLogger('sqlalchemy.dialects').setLevel(logging.WARNING)
logging.getLogger('sqlalchemy.orm').setLevel(logging.WARNING)

DB_URL = f"mysql+pymysql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
engine = create_engine(DB_URL, echo=False)

SessionLocal = sessionmaker(bind=engine)

def get_session():
    return SessionLocal()

def init_db():
    from .models import Base
    Base.metadata.create_all(engine)
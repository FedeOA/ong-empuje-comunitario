from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from .config import DB_USER, DB_PASS, DB_HOST, DB_PORT, DB_NAME

DB_URL = f"mysql+pymysql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
engine = create_engine(DB_URL, echo=True)

SessionLocal = sessionmaker(bind=engine)

def get_session():
    try:
        session = SessionLocal()
        print(f"Database session created successfully with URL: {DB_URL}")
        return session
    except Exception as e:
        print(f"Error creating database session: {str(e)}")
        raise

def init_db():
    from .models import Base
    Base.metadata.create_all(engine)
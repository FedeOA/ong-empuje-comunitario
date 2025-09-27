# Database Models
from sqlalchemy import Column, Integer, String, Boolean, DateTime, ForeignKey
from sqlalchemy.orm import declarative_base
import datetime
from sqlalchemy.orm import relationship

Base = declarative_base()

class User(Base):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True)
    username = Column(String(255))
    first_name = Column(String(255))
    last_name = Column(String(255))
    phone = Column(String(255))
    password_hash = Column(String(255))
    email = Column(String(255))
    is_active = Column(Boolean)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
    role_id = Column(Integer, ForeignKey('roles.id'))
    user_events = relationship("UserEvent", back_populates="user")

class Donation(Base):
    __tablename__ = 'donations'
    id = Column(Integer, primary_key=True)
    description = Column(String(255))
    quantity = Column(Integer)
    is_deleted = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
    created_by = Column(Integer, ForeignKey('users.id'))
    updated_at = Column(DateTime)
    updated_by = Column(Integer, ForeignKey('users.id'))
    category_id = Column(Integer, ForeignKey('categories.id'))
    user_id = Column(Integer, ForeignKey('users.id'))

class Event(Base):
    __tablename__ = 'events'
    id = Column(Integer, primary_key=True)
    name = Column(String(255))
    description = Column(String(255))
    event_datetime = Column(DateTime)
    user_events = relationship("UserEvent", back_populates="event")

class Role(Base):
    __tablename__ = 'roles'
    id = Column(Integer, primary_key=True)
    name = Column(String(255))

class Category(Base):
    __tablename__ = 'categories'
    id = Column(Integer, primary_key=True)
    name = Column(String(255))

class UserEvent(Base):
    __tablename__ = 'user_events'
    id = Column(Integer, primary_key=True)
    registration_date = Column(DateTime, default=datetime.datetime.utcnow)
    user_id = Column(Integer, ForeignKey('users.id'))
    event_id = Column(Integer, ForeignKey('events.id'))
    user = relationship("User", back_populates="user_events")
    event = relationship("Event", back_populates="user_events")

class EventDonation(Base):
    __tablename__ = 'event_donations'
    id = Column(Integer, primary_key=True)
    quantity_used = Column(Integer)
    event_id = Column(Integer, ForeignKey('events.id'))
    donation_id = Column(Integer, ForeignKey('donations.id'))
from sqlalchemy import Column, String, Boolean, DateTime, Integer, ForeignKey
from sqlalchemy.dialects.postgresql import ENUM, UUID
from sqlalchemy.orm import relationship
import enum
from datetime import datetime
import sqlalchemy
from db.database import Base

class AuthProvider(enum.Enum):
    google = "google"
 

class User(Base):
    __tablename__ = "users"
    
    id = Column(UUID(as_uuid=True), primary_key=True, default=sqlalchemy.text("uuid_generate_v4()"))
    name = Column(String)
    role = Column(String, nullable=True)
    first_name = Column(String)
    last_name = Column(String)
    email = Column(String, unique=True, index=True)
    image_url = Column(String, nullable=True)
    email_verified = Column(Boolean, default=False)
    provider = Column(ENUM(AuthProvider), nullable=False)
    is_expired = Column(Boolean, default=False)
    is_locked = Column(Boolean, default=False)
    is_credentials_expired = Column(Boolean, default=False)
    is_enabled = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class Bank(Base):
    __tablename__ = 'banks'
    
    bank_id = Column(Integer, primary_key=True, autoincrement=True)
    bank_name = Column(String, nullable=False)
    
    # Relationship - Points backref
    points = relationship('Points', back_populates='bank')

class Points(Base):
    __tablename__ = 'points'
    
    points_id = Column(UUID(as_uuid=True), primary_key=True, default=sqlalchemy.text("uuid_generate_v4()"))
    user_id = Column(UUID(as_uuid=True), ForeignKey('users.id'), nullable=False)
    points_balance = Column(Integer, nullable=False)
    bank_id = Column(Integer, ForeignKey('banks.bank_id'), nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    user = relationship('User', back_populates='points')
    bank = relationship('Bank', back_populates='points')

User.points = relationship('Points', back_populates='user')
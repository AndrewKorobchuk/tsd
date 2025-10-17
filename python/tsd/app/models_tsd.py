from sqlalchemy import Column, Integer, String, DateTime, Boolean, Text
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.database import Base

class TsdDevice(Base):
    __tablename__ = "tsd_devices"

    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(String(255), unique=True, index=True, nullable=False)
    device_name = Column(String(255))
    device_model = Column(String(255))
    android_version = Column(String(50))
    app_version = Column(String(50))
    prefix = Column(String(10), unique=True, index=True, nullable=False)
    is_active = Column(Boolean, default=True)
    document_counter = Column(Integer, default=0, nullable=False)  # Счетчик документов для инкрементной нумерации
    last_seen = Column(DateTime(timezone=True), server_default=func.now())
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())


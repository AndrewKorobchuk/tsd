from pydantic import BaseModel
from typing import Optional
from datetime import datetime

class TsdDeviceBase(BaseModel):
    device_id: str
    device_name: Optional[str] = None
    device_model: Optional[str] = None
    android_version: Optional[str] = None
    app_version: Optional[str] = None

class TsdDeviceCreate(TsdDeviceBase):
    pass

class TsdDeviceUpdate(TsdDeviceBase):
    pass

class TsdDevice(TsdDeviceBase):
    id: int
    prefix: str
    is_active: bool
    document_counter: int = 0
    last_seen: Optional[datetime] = None
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None

    class Config:
        from_attributes = True

class TsdDeviceRegisterRequest(BaseModel):
    device_id: str
    device_name: str
    device_model: str
    android_version: str
    app_version: str

class TsdDeviceRegisterResponse(BaseModel):
    id: int
    device_id: str
    prefix: str
    is_active: bool

class DocumentNumberRequest(BaseModel):
    device_id: str
    document_type: str = "input_balance"  # Тип документа (ввод остатков)

class DocumentNumberResponse(BaseModel):
    document_number: str
    next_counter: int


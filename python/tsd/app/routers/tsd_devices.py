from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models_tsd import TsdDevice
from app.schemas_tsd import (
    TsdDeviceCreate, 
    TsdDeviceUpdate, 
    TsdDevice as TsdDeviceSchema,
    TsdDeviceRegisterRequest,
    TsdDeviceRegisterResponse,
    DocumentNumberRequest,
    DocumentNumberResponse
)
from app.oauth import get_current_user_from_token
from app.models import User
import string
import random

router = APIRouter(prefix="/tsd-devices", tags=["tsd-devices"])

def generate_prefix() -> str:
    """Генерирует уникальный префикс для ТСД"""
    # Генерируем префикс вида "ТСД001", "ТСД002", etc.
    letters = "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЭЮЯ"
    numbers = "0123456789"
    
    # Находим следующий доступный номер
    db = next(get_db())
    try:
        existing_prefixes = db.query(TsdDevice.prefix).all()
        used_numbers = set()
        
        for prefix_tuple in existing_prefixes:
            prefix = prefix_tuple[0]
            if prefix.startswith("ТСД") and len(prefix) == 6:
                try:
                    number = int(prefix[3:])
                    used_numbers.add(number)
                except ValueError:
                    continue
        
        # Находим первый свободный номер
        next_number = 1
        while next_number in used_numbers:
            next_number += 1
        
        return f"ТСД{next_number:03d}"
    finally:
        db.close()

@router.post("/register", response_model=TsdDeviceRegisterResponse)
async def register_device(
    request: TsdDeviceRegisterRequest,
    current_user: User = Depends(get_current_user_from_token),
    db: Session = Depends(get_db)
):
    """Регистрация нового ТСД устройства"""
    
    # Проверяем, не зарегистрировано ли уже устройство
    existing_device = db.query(TsdDevice).filter(
        TsdDevice.device_id == request.device_id
    ).first()
    
    if existing_device:
        # Устройство уже зарегистрировано, возвращаем существующую информацию
        return TsdDeviceRegisterResponse(
            id=existing_device.id,
            device_id=existing_device.device_id,
            prefix=existing_device.prefix,
            is_active=existing_device.is_active
        )
    
    # Генерируем уникальный префикс
    prefix = generate_prefix()
    
    # Создаем новое устройство
    device = TsdDevice(
        device_id=request.device_id,
        device_name=request.device_name,
        device_model=request.device_model,
        android_version=request.android_version,
        app_version=request.app_version,
        prefix=prefix,
        is_active=True
    )
    
    db.add(device)
    db.commit()
    db.refresh(device)
    
    return TsdDeviceRegisterResponse(
        id=device.id,
        device_id=device.device_id,
        prefix=device.prefix,
        is_active=device.is_active
    )

@router.get("/me", response_model=TsdDeviceSchema)
async def get_my_device(
    current_user: User = Depends(get_current_user_from_token),
    db: Session = Depends(get_db)
):
    """Получение информации о текущем устройстве"""
    
    # Ищем устройство по device_id из заголовка или другим способом
    # Пока что возвращаем первое найденное устройство
    device = db.query(TsdDevice).first()
    
    if not device:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Device not found"
        )
    
    return device

@router.get("/", response_model=list[TsdDeviceSchema])
async def get_devices(
    skip: int = 0,
    limit: int = 100,
    active_only: bool = True,
    current_user: User = Depends(get_current_user_from_token),
    db: Session = Depends(get_db)
):
    """Получение списка всех ТСД устройств"""
    
    query = db.query(TsdDevice)
    
    if active_only:
        query = query.filter(TsdDevice.is_active == True)
    
    devices = query.offset(skip).limit(limit).all()
    return devices

@router.put("/me", response_model=TsdDeviceSchema)
async def update_my_device(
    request: TsdDeviceRegisterRequest,
    current_user: User = Depends(get_current_user_from_token),
    db: Session = Depends(get_db)
):
    """Обновление информации о текущем устройстве"""
    
    device = db.query(TsdDevice).filter(
        TsdDevice.device_id == request.device_id
    ).first()
    
    if not device:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Device not found"
        )
    
    # Обновляем информацию
    device.device_name = request.device_name
    device.device_model = request.device_model
    device.android_version = request.android_version
    device.app_version = request.app_version
    
    db.commit()
    db.refresh(device)
    
    return device

@router.post("/next-document-number", response_model=DocumentNumberResponse)
async def get_next_document_number(
    request: DocumentNumberRequest,
    current_user: User = Depends(get_current_user_from_token),
    db: Session = Depends(get_db)
):
    """Получение следующего номера документа для ТСД устройства"""
    
    # Находим устройство по device_id
    device = db.query(TsdDevice).filter(
        TsdDevice.device_id == request.device_id
    ).first()
    
    if not device:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="ТСД устройство не найдено"
        )
    
    if not device.is_active:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="ТСД устройство неактивно"
        )
    
    # Увеличиваем счетчик документов
    device.document_counter += 1
    db.commit()
    
    # Формируем номер документа
    document_number = f"{device.prefix}-{request.document_type.upper()}-{device.document_counter:06d}"
    
    return DocumentNumberResponse(
        document_number=document_number,
        next_counter=device.document_counter
    )


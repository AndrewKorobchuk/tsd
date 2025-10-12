from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import and_
from app.database import get_db
from app.models import Barcode, Nomenclature, User
from app.schemas import (
    Barcode as BarcodeSchema,
    BarcodeCreate,
    BarcodeUpdate
)
from app.oauth import get_current_user_from_token

router = APIRouter(prefix="/barcodes", tags=["barcodes"])


@router.get("/", response_model=List[BarcodeSchema])
async def get_barcodes(
    skip: int = Query(0, ge=0, description="Количество записей для пропуска"),
    limit: int = Query(100, ge=1, le=1000, description="Максимальное количество записей"),
    nomenclature_id: Optional[int] = Query(None, description="Фильтр по номенклатуре"),
    barcode_type: Optional[str] = Query(None, description="Фильтр по типу штрих-кода"),
    active_only: bool = Query(True, description="Показывать только активные штрих-коды"),
    search: Optional[str] = Query(None, description="Поиск по штрих-коду"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение списка штрих-кодов"""
    query = db.query(Barcode).options(
        joinedload(Barcode.nomenclature)
    )
    
    if nomenclature_id:
        query = query.filter(Barcode.nomenclature_id == nomenclature_id)
    
    if barcode_type:
        query = query.filter(Barcode.barcode_type == barcode_type)
    
    if active_only:
        query = query.filter(Barcode.is_active == True)
    
    if search:
        search_filter = f"%{search}%"
        query = query.filter(Barcode.barcode.ilike(search_filter))
    
    barcodes = query.offset(skip).limit(limit).all()
    return barcodes


@router.get("/{barcode_id}", response_model=BarcodeSchema)
async def get_barcode(
    barcode_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение штрих-кода по ID"""
    barcode = db.query(Barcode).options(
        joinedload(Barcode.nomenclature)
    ).filter(Barcode.id == barcode_id).first()
    
    if not barcode:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Штрих-код не найден"
        )
    return barcode


@router.get("/scan/{barcode_value}", response_model=BarcodeSchema)
async def get_barcode_by_value(
    barcode_value: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение номенклатуры по штрих-коду (для сканирования)"""
    barcode = db.query(Barcode).options(
        joinedload(Barcode.nomenclature)
    ).filter(
        and_(
            Barcode.barcode == barcode_value,
            Barcode.is_active == True
        )
    ).first()
    
    if not barcode:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Штрих-код не найден или неактивен"
        )
    return barcode


@router.post("/", response_model=BarcodeSchema)
async def create_barcode(
    barcode_data: BarcodeCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Создание нового штрих-кода"""
    # Проверяем, существует ли штрих-код с таким значением
    existing_barcode = db.query(Barcode).filter(Barcode.barcode == barcode_data.barcode).first()
    if existing_barcode:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Штрих-код с таким значением уже существует"
        )
    
    # Проверяем, существует ли номенклатура
    nomenclature = db.query(Nomenclature).filter(Nomenclature.id == barcode_data.nomenclature_id).first()
    if not nomenclature:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Номенклатура не найдена"
        )
    
    # Если это основной штрих-код, снимаем флаг с других штрих-кодов этой номенклатуры
    if barcode_data.is_primary:
        db.query(Barcode).filter(
            and_(
                Barcode.nomenclature_id == barcode_data.nomenclature_id,
                Barcode.is_primary == True
            )
        ).update({"is_primary": False})
    
    # Создаем новый штрих-код
    barcode = Barcode(
        nomenclature_id=barcode_data.nomenclature_id,
        barcode=barcode_data.barcode,
        barcode_type=barcode_data.barcode_type,
        is_primary=barcode_data.is_primary,
        is_active=barcode_data.is_active,
        description=barcode_data.description
    )
    
    db.add(barcode)
    db.commit()
    db.refresh(barcode)
    
    # Загружаем связанные данные для ответа
    barcode = db.query(Barcode).options(
        joinedload(Barcode.nomenclature)
    ).filter(Barcode.id == barcode.id).first()
    
    return barcode


@router.put("/{barcode_id}", response_model=BarcodeSchema)
async def update_barcode(
    barcode_id: int,
    barcode_data: BarcodeUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Обновление штрих-кода"""
    barcode = db.query(Barcode).filter(Barcode.id == barcode_id).first()
    if not barcode:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Штрих-код не найден"
        )
    
    # Проверяем уникальность штрих-кода, если он изменился
    if barcode_data.barcode and barcode_data.barcode != barcode.barcode:
        existing_barcode = db.query(Barcode).filter(
            and_(
                Barcode.barcode == barcode_data.barcode,
                Barcode.id != barcode_id
            )
        ).first()
        if existing_barcode:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Штрих-код с таким значением уже существует"
            )
    
    # Если это основной штрих-код, снимаем флаг с других штрих-кодов этой номенклатуры
    if barcode_data.is_primary:
        nomenclature_id = barcode_data.nomenclature_id or barcode.nomenclature_id
        db.query(Barcode).filter(
            and_(
                Barcode.nomenclature_id == nomenclature_id,
                Barcode.is_primary == True,
                Barcode.id != barcode_id
            )
        ).update({"is_primary": False})
    
    # Обновляем поля
    update_data = barcode_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(barcode, field, value)
    
    db.commit()
    db.refresh(barcode)
    
    # Загружаем связанные данные для ответа
    barcode = db.query(Barcode).options(
        joinedload(Barcode.nomenclature)
    ).filter(Barcode.id == barcode.id).first()
    
    return barcode


@router.delete("/{barcode_id}")
async def delete_barcode(
    barcode_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Удаление штрих-кода"""
    barcode = db.query(Barcode).filter(Barcode.id == barcode_id).first()
    if not barcode:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Штрих-код не найден"
        )
    
    db.delete(barcode)
    db.commit()
    
    return {"message": "Штрих-код удален"}


@router.patch("/{barcode_id}/activate")
async def activate_barcode(
    barcode_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Активация штрих-кода"""
    barcode = db.query(Barcode).filter(Barcode.id == barcode_id).first()
    if not barcode:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Штрих-код не найден"
        )
    
    barcode.is_active = True
    db.commit()
    
    return {"message": "Штрих-код активирован"}


@router.patch("/{barcode_id}/deactivate")
async def deactivate_barcode(
    barcode_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Деактивация штрих-кода"""
    barcode = db.query(Barcode).filter(Barcode.id == barcode_id).first()
    if not barcode:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Штрих-код не найден"
        )
    
    barcode.is_active = False
    db.commit()
    
    return {"message": "Штрих-код деактивирован"}


@router.patch("/{barcode_id}/set-primary")
async def set_primary_barcode(
    barcode_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Установка штрих-кода как основного"""
    barcode = db.query(Barcode).filter(Barcode.id == barcode_id).first()
    if not barcode:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Штрих-код не найден"
        )
    
    # Снимаем флаг основного с других штрих-кодов этой номенклатуры
    db.query(Barcode).filter(
        and_(
            Barcode.nomenclature_id == barcode.nomenclature_id,
            Barcode.is_primary == True,
            Barcode.id != barcode_id
        )
    ).update({"is_primary": False})
    
    # Устанавливаем флаг основного для текущего штрих-кода
    barcode.is_primary = True
    db.commit()
    
    return {"message": "Штрих-код установлен как основной"}

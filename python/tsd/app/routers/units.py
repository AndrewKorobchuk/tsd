from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from app.database import get_db
from app.models import UnitOfMeasure
from app.schemas import (
    UnitOfMeasure as UnitOfMeasureSchema,
    UnitOfMeasureCreate,
    UnitOfMeasureUpdate
)
from app.oauth import get_current_user_from_token
from app.models import User

router = APIRouter(prefix="/units", tags=["units-of-measure"])


@router.get("/", response_model=List[UnitOfMeasureSchema])
async def get_units_of_measure(
    skip: int = Query(0, ge=0, description="Количество записей для пропуска"),
    limit: int = Query(100, ge=1, le=1000, description="Максимальное количество записей"),
    active_only: bool = Query(True, description="Показывать только активные единицы измерения"),
    search: Optional[str] = Query(None, description="Поиск по названию или коду"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение списка единиц измерения"""
    query = db.query(UnitOfMeasure)
    
    if active_only:
        query = query.filter(UnitOfMeasure.is_active == True)
    
    if search:
        search_filter = f"%{search}%"
        query = query.filter(
            (UnitOfMeasure.name.ilike(search_filter)) |
            (UnitOfMeasure.code.ilike(search_filter)) |
            (UnitOfMeasure.short_name.ilike(search_filter))
        )
    
    units = query.offset(skip).limit(limit).all()
    return units


@router.get("/{unit_id}", response_model=UnitOfMeasureSchema)
async def get_unit_of_measure(
    unit_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение единицы измерения по ID"""
    unit = db.query(UnitOfMeasure).filter(UnitOfMeasure.id == unit_id).first()
    if not unit:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Единица измерения не найдена"
        )
    return unit


@router.get("/code/{code}", response_model=UnitOfMeasureSchema)
async def get_unit_by_code(
    code: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение единицы измерения по коду"""
    unit = db.query(UnitOfMeasure).filter(UnitOfMeasure.code == code).first()
    if not unit:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Единица измерения не найдена"
        )
    return unit


@router.post("/", response_model=UnitOfMeasureSchema)
async def create_unit_of_measure(
    unit_data: UnitOfMeasureCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Создание новой единицы измерения"""
    # Проверяем, существует ли единица с таким кодом
    existing_unit = db.query(UnitOfMeasure).filter(UnitOfMeasure.code == unit_data.code).first()
    if existing_unit:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Единица измерения с таким кодом уже существует"
        )
    
    # Создаем новую единицу измерения
    unit = UnitOfMeasure(
        code=unit_data.code,
        name=unit_data.name,
        short_name=unit_data.short_name,
        description=unit_data.description,
        is_active=unit_data.is_active
    )
    
    db.add(unit)
    db.commit()
    db.refresh(unit)
    
    return unit


@router.put("/{unit_id}", response_model=UnitOfMeasureSchema)
async def update_unit_of_measure(
    unit_id: int,
    unit_data: UnitOfMeasureUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Обновление единицы измерения"""
    unit = db.query(UnitOfMeasure).filter(UnitOfMeasure.id == unit_id).first()
    if not unit:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Единица измерения не найдена"
        )
    
    # Проверяем, не занят ли новый код другой единицей
    if unit_data.code and unit_data.code != unit.code:
        existing_unit = db.query(UnitOfMeasure).filter(
            UnitOfMeasure.code == unit_data.code,
            UnitOfMeasure.id != unit_id
        ).first()
        if existing_unit:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Единица измерения с таким кодом уже существует"
            )
    
    # Обновляем поля
    update_data = unit_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(unit, field, value)
    
    db.commit()
    db.refresh(unit)
    
    return unit


@router.delete("/{unit_id}")
async def delete_unit_of_measure(
    unit_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Удаление единицы измерения (мягкое удаление - деактивация)"""
    unit = db.query(UnitOfMeasure).filter(UnitOfMeasure.id == unit_id).first()
    if not unit:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Единица измерения не найдена"
        )
    
    # Мягкое удаление - деактивируем единицу измерения
    unit.is_active = False
    db.commit()
    
    return {"message": "Единица измерения деактивирована"}


@router.patch("/{unit_id}/activate")
async def activate_unit_of_measure(
    unit_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Активация единицы измерения"""
    unit = db.query(UnitOfMeasure).filter(UnitOfMeasure.id == unit_id).first()
    if not unit:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Единица измерения не найдена"
        )
    
    unit.is_active = True
    db.commit()
    
    return {"message": "Единица измерения активирована"}

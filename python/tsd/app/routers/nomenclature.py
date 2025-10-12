from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session, joinedload
from app.database import get_db
from app.models import Nomenclature, NomenclatureCategory, UnitOfMeasure
from app.schemas import (
    Nomenclature as NomenclatureSchema,
    NomenclatureCreate,
    NomenclatureUpdate
)
from app.oauth import get_current_user_from_token
from app.models import User

router = APIRouter(prefix="/nomenclature", tags=["nomenclature"])


@router.get("/", response_model=List[NomenclatureSchema])
async def get_nomenclature(
    skip: int = Query(0, ge=0, description="Количество записей для пропуска"),
    limit: int = Query(100, ge=1, le=1000, description="Максимальное количество записей"),
    active_only: bool = Query(True, description="Показывать только активную номенклатуру"),
    category_id: Optional[int] = Query(None, description="Фильтр по категории"),
    search: Optional[str] = Query(None, description="Поиск по названию или коду"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение списка номенклатуры"""
    query = db.query(Nomenclature).options(
        joinedload(Nomenclature.category),
        joinedload(Nomenclature.base_unit)
    )
    
    if active_only:
        query = query.filter(Nomenclature.is_active == True)
    
    if category_id:
        query = query.filter(Nomenclature.category_id == category_id)
    
    if search:
        search_filter = f"%{search}%"
        query = query.filter(
            (Nomenclature.name.ilike(search_filter)) |
            (Nomenclature.code.ilike(search_filter)) |
            (Nomenclature.description_ru.ilike(search_filter)) |
            (Nomenclature.description_ua.ilike(search_filter))
        )
    
    nomenclature = query.offset(skip).limit(limit).all()
    return nomenclature


@router.get("/{nomenclature_id}", response_model=NomenclatureSchema)
async def get_nomenclature_item(
    nomenclature_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение номенклатуры по ID"""
    nomenclature = db.query(Nomenclature).options(
        joinedload(Nomenclature.category),
        joinedload(Nomenclature.base_unit)
    ).filter(Nomenclature.id == nomenclature_id).first()
    
    if not nomenclature:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Номенклатура не найдена"
        )
    return nomenclature


@router.get("/code/{code}", response_model=NomenclatureSchema)
async def get_nomenclature_by_code(
    code: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение номенклатуры по коду"""
    nomenclature = db.query(Nomenclature).options(
        joinedload(Nomenclature.category),
        joinedload(Nomenclature.base_unit)
    ).filter(Nomenclature.code == code).first()
    
    if not nomenclature:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Номенклатура не найдена"
        )
    return nomenclature


@router.post("/", response_model=NomenclatureSchema)
async def create_nomenclature(
    nomenclature_data: NomenclatureCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Создание новой номенклатуры"""
    # Проверяем, существует ли номенклатура с таким кодом
    existing_nomenclature = db.query(Nomenclature).filter(Nomenclature.code == nomenclature_data.code).first()
    if existing_nomenclature:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Номенклатура с таким кодом уже существует"
        )
    
    # Проверяем, существует ли категория
    category = db.query(NomenclatureCategory).filter(NomenclatureCategory.id == nomenclature_data.category_id).first()
    if not category:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Категория номенклатуры не найдена"
        )
    
    # Проверяем, существует ли единица измерения
    unit = db.query(UnitOfMeasure).filter(UnitOfMeasure.id == nomenclature_data.base_unit_id).first()
    if not unit:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Единица измерения не найдена"
        )
    
    # Создаем новую номенклатуру
    nomenclature = Nomenclature(
        code=nomenclature_data.code,
        category_id=nomenclature_data.category_id,
        name=nomenclature_data.name,
        base_unit_id=nomenclature_data.base_unit_id,
        description_ru=nomenclature_data.description_ru,
        description_ua=nomenclature_data.description_ua,
        is_active=nomenclature_data.is_active
    )
    
    db.add(nomenclature)
    db.commit()
    db.refresh(nomenclature)
    
    # Загружаем связанные данные для ответа
    nomenclature = db.query(Nomenclature).options(
        joinedload(Nomenclature.category),
        joinedload(Nomenclature.base_unit)
    ).filter(Nomenclature.id == nomenclature.id).first()
    
    return nomenclature


@router.put("/{nomenclature_id}", response_model=NomenclatureSchema)
async def update_nomenclature(
    nomenclature_id: int,
    nomenclature_data: NomenclatureUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Обновление номенклатуры"""
    nomenclature = db.query(Nomenclature).filter(Nomenclature.id == nomenclature_id).first()
    if not nomenclature:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Номенклатура не найдена"
        )
    
    # Проверяем, не занят ли новый код другой номенклатурой
    if nomenclature_data.code and nomenclature_data.code != nomenclature.code:
        existing_nomenclature = db.query(Nomenclature).filter(
            Nomenclature.code == nomenclature_data.code,
            Nomenclature.id != nomenclature_id
        ).first()
        if existing_nomenclature:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Номенклатура с таким кодом уже существует"
            )
    
    # Проверяем категорию, если она указана
    if nomenclature_data.category_id:
        category = db.query(NomenclatureCategory).filter(NomenclatureCategory.id == nomenclature_data.category_id).first()
        if not category:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Категория номенклатуры не найдена"
            )
    
    # Проверяем единицу измерения, если она указана
    if nomenclature_data.base_unit_id:
        unit = db.query(UnitOfMeasure).filter(UnitOfMeasure.id == nomenclature_data.base_unit_id).first()
        if not unit:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Единица измерения не найдена"
            )
    
    # Обновляем поля
    update_data = nomenclature_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(nomenclature, field, value)
    
    db.commit()
    db.refresh(nomenclature)
    
    # Загружаем связанные данные для ответа
    nomenclature = db.query(Nomenclature).options(
        joinedload(Nomenclature.category),
        joinedload(Nomenclature.base_unit)
    ).filter(Nomenclature.id == nomenclature.id).first()
    
    return nomenclature


@router.delete("/{nomenclature_id}")
async def delete_nomenclature(
    nomenclature_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Удаление номенклатуры (мягкое удаление - деактивация)"""
    nomenclature = db.query(Nomenclature).filter(Nomenclature.id == nomenclature_id).first()
    if not nomenclature:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Номенклатура не найдена"
        )
    
    # Мягкое удаление - деактивируем номенклатуру
    nomenclature.is_active = False
    db.commit()
    
    return {"message": "Номенклатура деактивирована"}


@router.patch("/{nomenclature_id}/activate")
async def activate_nomenclature(
    nomenclature_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Активация номенклатуры"""
    nomenclature = db.query(Nomenclature).filter(Nomenclature.id == nomenclature_id).first()
    if not nomenclature:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Номенклатура не найдена"
        )
    
    nomenclature.is_active = True
    db.commit()
    
    return {"message": "Номенклатура активирована"}

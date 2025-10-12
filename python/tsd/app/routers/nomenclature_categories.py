from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from app.database import get_db
from app.models import NomenclatureCategory
from app.schemas import (
    NomenclatureCategory as NomenclatureCategorySchema,
    NomenclatureCategoryCreate,
    NomenclatureCategoryUpdate
)
from app.oauth import get_current_user_from_token
from app.models import User

router = APIRouter(prefix="/nomenclature-categories", tags=["nomenclature-categories"])


@router.get("/", response_model=List[NomenclatureCategorySchema])
async def get_nomenclature_categories(
    skip: int = Query(0, ge=0, description="Количество записей для пропуска"),
    limit: int = Query(100, ge=1, le=1000, description="Максимальное количество записей"),
    active_only: bool = Query(True, description="Показывать только активные категории"),
    search: Optional[str] = Query(None, description="Поиск по названию или коду"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение списка категорий номенклатуры"""
    query = db.query(NomenclatureCategory)
    
    if active_only:
        query = query.filter(NomenclatureCategory.is_active == True)
    
    if search:
        search_filter = f"%{search}%"
        query = query.filter(
            (NomenclatureCategory.name.ilike(search_filter)) |
            (NomenclatureCategory.code.ilike(search_filter))
        )
    
    categories = query.offset(skip).limit(limit).all()
    return categories


@router.get("/{category_id}", response_model=NomenclatureCategorySchema)
async def get_nomenclature_category(
    category_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение категории номенклатуры по ID"""
    category = db.query(NomenclatureCategory).filter(NomenclatureCategory.id == category_id).first()
    if not category:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Категория номенклатуры не найдена"
        )
    return category


@router.get("/code/{code}", response_model=NomenclatureCategorySchema)
async def get_category_by_code(
    code: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение категории номенклатуры по коду"""
    category = db.query(NomenclatureCategory).filter(NomenclatureCategory.code == code).first()
    if not category:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Категория номенклатуры не найдена"
        )
    return category


@router.post("/", response_model=NomenclatureCategorySchema)
async def create_nomenclature_category(
    category_data: NomenclatureCategoryCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Создание новой категории номенклатуры"""
    # Проверяем, существует ли категория с таким кодом
    existing_category = db.query(NomenclatureCategory).filter(NomenclatureCategory.code == category_data.code).first()
    if existing_category:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Категория номенклатуры с таким кодом уже существует"
        )
    
    # Создаем новую категорию
    category = NomenclatureCategory(
        code=category_data.code,
        name=category_data.name,
        description=category_data.description,
        is_active=category_data.is_active
    )
    
    db.add(category)
    db.commit()
    db.refresh(category)
    
    return category


@router.put("/{category_id}", response_model=NomenclatureCategorySchema)
async def update_nomenclature_category(
    category_id: int,
    category_data: NomenclatureCategoryUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Обновление категории номенклатуры"""
    category = db.query(NomenclatureCategory).filter(NomenclatureCategory.id == category_id).first()
    if not category:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Категория номенклатуры не найдена"
        )
    
    # Проверяем, не занят ли новый код другой категорией
    if category_data.code and category_data.code != category.code:
        existing_category = db.query(NomenclatureCategory).filter(
            NomenclatureCategory.code == category_data.code,
            NomenclatureCategory.id != category_id
        ).first()
        if existing_category:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Категория номенклатуры с таким кодом уже существует"
            )
    
    # Обновляем поля
    update_data = category_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(category, field, value)
    
    db.commit()
    db.refresh(category)
    
    return category


@router.delete("/{category_id}")
async def delete_nomenclature_category(
    category_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Удаление категории номенклатуры (мягкое удаление - деактивация)"""
    category = db.query(NomenclatureCategory).filter(NomenclatureCategory.id == category_id).first()
    if not category:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Категория номенклатуры не найдена"
        )
    
    # Мягкое удаление - деактивируем категорию
    category.is_active = False
    db.commit()
    
    return {"message": "Категория номенклатуры деактивирована"}


@router.patch("/{category_id}/activate")
async def activate_nomenclature_category(
    category_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Активация категории номенклатуры"""
    category = db.query(NomenclatureCategory).filter(NomenclatureCategory.id == category_id).first()
    if not category:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Категория номенклатуры не найдена"
        )
    
    category.is_active = True
    db.commit()
    
    return {"message": "Категория номенклатуры активирована"}

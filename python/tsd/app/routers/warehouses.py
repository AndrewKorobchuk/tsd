from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from app.database import get_db
from app.models import Warehouse
from app.schemas import (
    WarehouseCreate,
    Warehouse as WarehouseSchema,
    WarehouseUpdate
)
from app.oauth import get_current_user_from_token # Защита эндпоинтов

router = APIRouter(prefix="/warehouses", tags=["warehouses"])


@router.post("/", response_model=WarehouseSchema, status_code=status.HTTP_201_CREATED)
async def create_warehouse(
    warehouse: WarehouseCreate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(get_current_user_from_token) # Защита эндпоинта
):
    """Создание нового склада"""
    db_warehouse = db.query(Warehouse).filter(Warehouse.code == warehouse.code).first()
    if db_warehouse:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Склад с таким кодом уже существует")
    
    db_warehouse = Warehouse(**warehouse.model_dump())
    db.add(db_warehouse)
    db.commit()
    db.refresh(db_warehouse)
    return db_warehouse


@router.get("/", response_model=List[WarehouseSchema])
async def read_warehouses(
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=0, le=1000),
    active_only: bool = Query(True, description="Показывать только активные склады"),
    search: Optional[str] = Query(None, description="Поиск по коду, названию или адресу"),
    db: Session = Depends(get_db),
    current_user: dict = Depends(get_current_user_from_token) # Защита эндпоинта
):
    """Получение списка складов"""
    query = db.query(Warehouse)
    if active_only:
        query = query.filter(Warehouse.is_active == True)
    if search:
        query = query.filter(
            (Warehouse.code.ilike(f"%{search}%")) |
            (Warehouse.name.ilike(f"%{search}%")) |
            (Warehouse.address.ilike(f"%{search}%"))
        )
    warehouses = query.offset(skip).limit(limit).all()
    return warehouses


@router.get("/{warehouse_id}", response_model=WarehouseSchema)
async def read_warehouse(
    warehouse_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(get_current_user_from_token) # Защита эндпоинта
):
    """Получение склада по ID"""
    db_warehouse = db.query(Warehouse).filter(Warehouse.id == warehouse_id).first()
    if db_warehouse is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Склад не найден")
    return db_warehouse


@router.get("/code/{warehouse_code}", response_model=WarehouseSchema)
async def read_warehouse_by_code(
    warehouse_code: str,
    db: Session = Depends(get_db),
    current_user: dict = Depends(get_current_user_from_token) # Защита эндпоинта
):
    """Получение склада по коду"""
    db_warehouse = db.query(Warehouse).filter(Warehouse.code == warehouse_code).first()
    if db_warehouse is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Склад не найден")
    return db_warehouse


@router.put("/{warehouse_id}", response_model=WarehouseSchema)
async def update_warehouse(
    warehouse_id: int,
    warehouse: WarehouseUpdate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(get_current_user_from_token) # Защита эндпоинта
):
    """Обновление склада"""
    db_warehouse = db.query(Warehouse).filter(Warehouse.id == warehouse_id).first()
    if db_warehouse is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Склад не найден")
    
    update_data = warehouse.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_warehouse, key, value)
    
    db.add(db_warehouse)
    db.commit()
    db.refresh(db_warehouse)
    return db_warehouse


@router.delete("/{warehouse_id}", response_model=WarehouseSchema)
async def deactivate_warehouse(
    warehouse_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(get_current_user_from_token) # Защита эндпоинта
):
    """Деактивация склада (мягкое удаление)"""
    db_warehouse = db.query(Warehouse).filter(Warehouse.id == warehouse_id).first()
    if db_warehouse is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Склад не найден")
    
    db_warehouse.is_active = False
    db.add(db_warehouse)
    db.commit()
    db.refresh(db_warehouse)
    return db_warehouse


@router.patch("/{warehouse_id}/activate", response_model=WarehouseSchema)
async def activate_warehouse(
    warehouse_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(get_current_user_from_token) # Защита эндпоинта
):
    """Активация склада"""
    db_warehouse = db.query(Warehouse).filter(Warehouse.id == warehouse_id).first()
    if db_warehouse is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Склад не найден")
    
    db_warehouse.is_active = True
    db.add(db_warehouse)
    db.commit()
    db.refresh(db_warehouse)
    return db_warehouse

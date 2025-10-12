from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import and_
from app.database import get_db
from app.models import Stock, Nomenclature, Warehouse, User
from app.schemas import (
    Stock as StockSchema,
    StockCreate,
    StockUpdate,
    StockSummary
)
from app.oauth import get_current_user_from_token

router = APIRouter(prefix="/stocks", tags=["stocks"])


@router.get("/", response_model=List[StockSchema])
async def get_stocks(
    skip: int = Query(0, ge=0, description="Количество записей для пропуска"),
    limit: int = Query(100, ge=1, le=1000, description="Максимальное количество записей"),
    warehouse_id: Optional[int] = Query(None, description="Фильтр по складу"),
    nomenclature_id: Optional[int] = Query(None, description="Фильтр по номенклатуре"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение списка остатков товаров"""
    query = db.query(Stock).options(
        joinedload(Stock.nomenclature),
        joinedload(Stock.warehouse)
    )
    
    if warehouse_id:
        query = query.filter(Stock.warehouse_id == warehouse_id)
    
    if nomenclature_id:
        query = query.filter(Stock.nomenclature_id == nomenclature_id)
    
    stocks = query.offset(skip).limit(limit).all()
    return stocks


@router.get("/summary", response_model=List[StockSummary])
async def get_stocks_summary(
    warehouse_id: Optional[int] = Query(None, description="Фильтр по складу"),
    nomenclature_id: Optional[int] = Query(None, description="Фильтр по номенклатуре"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение сводки по остаткам товаров"""
    query = db.query(
        Stock.nomenclature_id,
        Nomenclature.code.label('nomenclature_code'),
        Nomenclature.name.label('nomenclature_name'),
        Stock.warehouse_id,
        Warehouse.code.label('warehouse_code'),
        Warehouse.name.label('warehouse_name'),
        Stock.quantity,
        Stock.reserved_quantity,
        (Stock.quantity - Stock.reserved_quantity).label('available_quantity'),
        Stock.last_updated
    ).join(
        Nomenclature, Stock.nomenclature_id == Nomenclature.id
    ).join(
        Warehouse, Stock.warehouse_id == Warehouse.id
    )
    
    if warehouse_id:
        query = query.filter(Stock.warehouse_id == warehouse_id)
    
    if nomenclature_id:
        query = query.filter(Stock.nomenclature_id == nomenclature_id)
    
    results = query.all()
    
    return [
        StockSummary(
            nomenclature_id=row.nomenclature_id,
            nomenclature_code=row.nomenclature_code,
            nomenclature_name=row.nomenclature_name,
            warehouse_id=row.warehouse_id,
            warehouse_code=row.warehouse_code,
            warehouse_name=row.warehouse_name,
            quantity=row.quantity,
            reserved_quantity=row.reserved_quantity,
            available_quantity=row.available_quantity,
            last_updated=row.last_updated
        )
        for row in results
    ]


@router.get("/{stock_id}", response_model=StockSchema)
async def get_stock(
    stock_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение остатка по ID"""
    stock = db.query(Stock).options(
        joinedload(Stock.nomenclature),
        joinedload(Stock.warehouse)
    ).filter(Stock.id == stock_id).first()
    
    if not stock:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Остаток не найден"
        )
    return stock


@router.get("/nomenclature/{nomenclature_id}/warehouse/{warehouse_id}", response_model=StockSchema)
async def get_stock_by_nomenclature_and_warehouse(
    nomenclature_id: int,
    warehouse_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение остатка по номенклатуре и складу"""
    stock = db.query(Stock).options(
        joinedload(Stock.nomenclature),
        joinedload(Stock.warehouse)
    ).filter(
        and_(
            Stock.nomenclature_id == nomenclature_id,
            Stock.warehouse_id == warehouse_id
        )
    ).first()
    
    if not stock:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Остаток не найден"
        )
    return stock


@router.post("/", response_model=StockSchema)
async def create_stock(
    stock_data: StockCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Создание нового остатка"""
    # Проверяем, существует ли уже остаток для данной номенклатуры и склада
    existing_stock = db.query(Stock).filter(
        and_(
            Stock.nomenclature_id == stock_data.nomenclature_id,
            Stock.warehouse_id == stock_data.warehouse_id
        )
    ).first()
    
    if existing_stock:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Остаток для данной номенклатуры и склада уже существует"
        )
    
    # Проверяем, существует ли номенклатура
    nomenclature = db.query(Nomenclature).filter(Nomenclature.id == stock_data.nomenclature_id).first()
    if not nomenclature:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Номенклатура не найдена"
        )
    
    # Проверяем, существует ли склад
    warehouse = db.query(Warehouse).filter(Warehouse.id == stock_data.warehouse_id).first()
    if not warehouse:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Склад не найден"
        )
    
    # Создаем новый остаток
    stock = Stock(
        nomenclature_id=stock_data.nomenclature_id,
        warehouse_id=stock_data.warehouse_id,
        quantity=stock_data.quantity,
        reserved_quantity=stock_data.reserved_quantity
    )
    
    db.add(stock)
    db.commit()
    db.refresh(stock)
    
    # Загружаем связанные данные для ответа
    stock = db.query(Stock).options(
        joinedload(Stock.nomenclature),
        joinedload(Stock.warehouse)
    ).filter(Stock.id == stock.id).first()
    
    return stock


@router.put("/{stock_id}", response_model=StockSchema)
async def update_stock(
    stock_id: int,
    stock_data: StockUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Обновление остатка"""
    stock = db.query(Stock).filter(Stock.id == stock_id).first()
    if not stock:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Остаток не найден"
        )
    
    # Обновляем поля
    update_data = stock_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(stock, field, value)
    
    db.commit()
    db.refresh(stock)
    
    # Загружаем связанные данные для ответа
    stock = db.query(Stock).options(
        joinedload(Stock.nomenclature),
        joinedload(Stock.warehouse)
    ).filter(Stock.id == stock.id).first()
    
    return stock


@router.delete("/{stock_id}")
async def delete_stock(
    stock_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Удаление остатка"""
    stock = db.query(Stock).filter(Stock.id == stock_id).first()
    if not stock:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Остаток не найден"
        )
    
    db.delete(stock)
    db.commit()
    
    return {"message": "Остаток удален"}

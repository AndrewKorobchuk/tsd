from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import and_
from app.database import get_db
from app.models import (
    Inventory, InventoryItem, Nomenclature, Warehouse, User, UnitOfMeasure,
    InventoryStatus
)
from app.schemas import (
    Inventory as InventorySchema,
    InventoryCreate,
    InventoryUpdate,
    InventoryWithItems,
    InventoryItem as InventoryItemSchema,
    InventoryItemCreate,
    InventoryItemUpdate
)
from app.oauth import get_current_user_from_token

router = APIRouter(prefix="/inventories", tags=["inventories"])


@router.get("/", response_model=List[InventorySchema])
async def get_inventories(
    skip: int = Query(0, ge=0, description="Количество записей для пропуска"),
    limit: int = Query(100, ge=1, le=1000, description="Максимальное количество записей"),
    warehouse_id: Optional[int] = Query(None, description="Фильтр по складу"),
    status: Optional[InventoryStatus] = Query(None, description="Фильтр по статусу"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение списка инвентаризаций"""
    query = db.query(Inventory).options(
        joinedload(Inventory.warehouse),
        joinedload(Inventory.creator)
    )
    
    if warehouse_id:
        query = query.filter(Inventory.warehouse_id == warehouse_id)
    
    if status:
        query = query.filter(Inventory.status == status)
    
    inventories = query.offset(skip).limit(limit).all()
    return inventories


@router.get("/{inventory_id}", response_model=InventoryWithItems)
async def get_inventory(
    inventory_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение инвентаризации по ID с строками"""
    inventory = db.query(Inventory).options(
        joinedload(Inventory.warehouse),
        joinedload(Inventory.creator),
        joinedload(Inventory.items).joinedload(InventoryItem.nomenclature),
        joinedload(Inventory.items).joinedload(InventoryItem.unit),
        joinedload(Inventory.items).joinedload(InventoryItem.counter)
    ).filter(Inventory.id == inventory_id).first()
    
    if not inventory:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Инвентаризация не найдена"
        )
    return inventory


@router.post("/", response_model=InventorySchema)
async def create_inventory(
    inventory_data: InventoryCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Создание новой инвентаризации"""
    # Проверяем, существует ли склад
    warehouse = db.query(Warehouse).filter(Warehouse.id == inventory_data.warehouse_id).first()
    if not warehouse:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Склад не найден"
        )
    
    # Проверяем уникальность номера инвентаризации
    existing_inventory = db.query(Inventory).filter(
        Inventory.inventory_number == inventory_data.inventory_number
    ).first()
    if existing_inventory:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Инвентаризация с таким номером уже существует"
        )
    
    # Создаем новую инвентаризацию
    inventory = Inventory(
        warehouse_id=inventory_data.warehouse_id,
        inventory_number=inventory_data.inventory_number,
        date_start=inventory_data.date_start,
        date_end=inventory_data.date_end,
        status=inventory_data.status,
        created_by=current_user.id,
        description=inventory_data.description
    )
    
    db.add(inventory)
    db.commit()
    db.refresh(inventory)
    
    # Загружаем связанные данные для ответа
    inventory = db.query(Inventory).options(
        joinedload(Inventory.warehouse),
        joinedload(Inventory.creator)
    ).filter(Inventory.id == inventory.id).first()
    
    return inventory


@router.put("/{inventory_id}", response_model=InventorySchema)
async def update_inventory(
    inventory_id: int,
    inventory_data: InventoryUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Обновление инвентаризации"""
    inventory = db.query(Inventory).filter(Inventory.id == inventory_id).first()
    if not inventory:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Инвентаризация не найдена"
        )
    
    # Проверяем, можно ли редактировать инвентаризацию
    if inventory.status == InventoryStatus.COMPLETED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Нельзя редактировать завершенную инвентаризацию"
        )
    
    # Проверяем уникальность номера инвентаризации, если он изменился
    if inventory_data.inventory_number and inventory_data.inventory_number != inventory.inventory_number:
        existing_inventory = db.query(Inventory).filter(
            and_(
                Inventory.inventory_number == inventory_data.inventory_number,
                Inventory.id != inventory_id
            )
        ).first()
        if existing_inventory:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Инвентаризация с таким номером уже существует"
            )
    
    # Обновляем поля
    update_data = inventory_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(inventory, field, value)
    
    db.commit()
    db.refresh(inventory)
    
    # Загружаем связанные данные для ответа
    inventory = db.query(Inventory).options(
        joinedload(Inventory.warehouse),
        joinedload(Inventory.creator)
    ).filter(Inventory.id == inventory.id).first()
    
    return inventory


@router.delete("/{inventory_id}")
async def delete_inventory(
    inventory_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Удаление инвентаризации"""
    inventory = db.query(Inventory).filter(Inventory.id == inventory_id).first()
    if not inventory:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Инвентаризация не найдена"
        )
    
    # Проверяем, можно ли удалить инвентаризацию
    if inventory.status == InventoryStatus.COMPLETED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Нельзя удалить завершенную инвентаризацию"
        )
    
    db.delete(inventory)
    db.commit()
    
    return {"message": "Инвентаризация удалена"}


@router.patch("/{inventory_id}/complete")
async def complete_inventory(
    inventory_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Завершение инвентаризации"""
    inventory = db.query(Inventory).options(
        joinedload(Inventory.items)
    ).filter(Inventory.id == inventory_id).first()
    
    if not inventory:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Инвентаризация не найдена"
        )
    
    if inventory.status == InventoryStatus.COMPLETED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Инвентаризация уже завершена"
        )
    
    if not inventory.items:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Нельзя завершить инвентаризацию без строк"
        )
    
    # Проверяем, что все строки посчитаны
    uncounted_items = [item for item in inventory.items if item.actual_quantity is None]
    if uncounted_items:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Не все позиции посчитаны"
        )
    
    # Здесь должна быть логика завершения инвентаризации
    # (создание документов по разницам, обновление остатков и т.д.)
    # Пока просто меняем статус и дату окончания
    
    from datetime import datetime
    inventory.status = InventoryStatus.COMPLETED
    inventory.date_end = datetime.utcnow()
    db.commit()
    
    return {"message": "Инвентаризация завершена"}


@router.patch("/{inventory_id}/cancel")
async def cancel_inventory(
    inventory_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Отмена инвентаризации"""
    inventory = db.query(Inventory).filter(Inventory.id == inventory_id).first()
    
    if not inventory:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Инвентаризация не найдена"
        )
    
    if inventory.status == InventoryStatus.CANCELLED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Инвентаризация уже отменена"
        )
    
    inventory.status = InventoryStatus.CANCELLED
    db.commit()
    
    return {"message": "Инвентаризация отменена"}


# Эндпоинты для работы со строками инвентаризации
@router.post("/{inventory_id}/items", response_model=InventoryItemSchema)
async def create_inventory_item(
    inventory_id: int,
    item_data: InventoryItemCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Создание строки инвентаризации"""
    # Проверяем, существует ли инвентаризация
    inventory = db.query(Inventory).filter(Inventory.id == inventory_id).first()
    if not inventory:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Инвентаризация не найдена"
        )
    
    # Проверяем, можно ли редактировать инвентаризацию
    if inventory.status == InventoryStatus.COMPLETED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Нельзя редактировать завершенную инвентаризацию"
        )
    
    # Проверяем, существует ли номенклатура
    nomenclature = db.query(Nomenclature).filter(Nomenclature.id == item_data.nomenclature_id).first()
    if not nomenclature:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Номенклатура не найдена"
        )
    
    # Проверяем, существует ли единица измерения
    unit = db.query(UnitOfMeasure).filter(UnitOfMeasure.id == item_data.unit_id).first()
    if not unit:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Единица измерения не найдена"
        )
    
    # Проверяем, не существует ли уже строка для данной номенклатуры
    existing_item = db.query(InventoryItem).filter(
        and_(
            InventoryItem.inventory_id == inventory_id,
            InventoryItem.nomenclature_id == item_data.nomenclature_id
        )
    ).first()
    if existing_item:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Строка для данной номенклатуры уже существует"
        )
    
    # Создаем новую строку инвентаризации
    item = InventoryItem(
        inventory_id=inventory_id,
        nomenclature_id=item_data.nomenclature_id,
        planned_quantity=item_data.planned_quantity,
        actual_quantity=item_data.actual_quantity,
        difference=item_data.difference,
        unit_id=item_data.unit_id
    )
    
    db.add(item)
    db.commit()
    db.refresh(item)
    
    # Загружаем связанные данные для ответа
    item = db.query(InventoryItem).options(
        joinedload(InventoryItem.nomenclature),
        joinedload(InventoryItem.unit)
    ).filter(InventoryItem.id == item.id).first()
    
    return item


@router.put("/items/{item_id}", response_model=InventoryItemSchema)
async def update_inventory_item(
    item_id: int,
    item_data: InventoryItemUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Обновление строки инвентаризации"""
    item = db.query(InventoryItem).filter(InventoryItem.id == item_id).first()
    if not item:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Строка инвентаризации не найдена"
        )
    
    # Проверяем, можно ли редактировать инвентаризацию
    if item.inventory.status == InventoryStatus.COMPLETED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Нельзя редактировать завершенную инвентаризацию"
        )
    
    # Обновляем поля
    update_data = item_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(item, field, value)
    
    # Если указано фактическое количество, рассчитываем разницу
    if item_data.actual_quantity is not None:
        item.difference = item_data.actual_quantity - item.planned_quantity
        item.counted_by = current_user.id
        from datetime import datetime
        item.counted_at = datetime.utcnow()
    
    db.commit()
    db.refresh(item)
    
    # Загружаем связанные данные для ответа
    item = db.query(InventoryItem).options(
        joinedload(InventoryItem.nomenclature),
        joinedload(InventoryItem.unit),
        joinedload(InventoryItem.counter)
    ).filter(InventoryItem.id == item.id).first()
    
    return item


@router.delete("/items/{item_id}")
async def delete_inventory_item(
    item_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Удаление строки инвентаризации"""
    item = db.query(InventoryItem).filter(InventoryItem.id == item_id).first()
    if not item:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Строка инвентаризации не найдена"
        )
    
    # Проверяем, можно ли редактировать инвентаризацию
    if item.inventory.status == InventoryStatus.COMPLETED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Нельзя редактировать завершенную инвентаризацию"
        )
    
    db.delete(item)
    db.commit()
    
    return {"message": "Строка инвентаризации удалена"}

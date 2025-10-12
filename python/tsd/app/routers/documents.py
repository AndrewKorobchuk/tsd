from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import and_
from app.database import get_db
from app.models import (
    Document, DocumentItem, Nomenclature, Warehouse, User, UnitOfMeasure,
    DocumentType, DocumentStatus
)
from app.schemas import (
    Document as DocumentSchema,
    DocumentCreate,
    DocumentUpdate,
    DocumentWithItems,
    DocumentItem as DocumentItemSchema,
    DocumentItemCreate,
    DocumentItemUpdate
)
from app.oauth import get_current_user_from_token

router = APIRouter(prefix="/documents", tags=["documents"])


@router.get("/", response_model=List[DocumentSchema])
async def get_documents(
    skip: int = Query(0, ge=0, description="Количество записей для пропуска"),
    limit: int = Query(100, ge=1, le=1000, description="Максимальное количество записей"),
    document_type: Optional[DocumentType] = Query(None, description="Фильтр по типу документа"),
    warehouse_id: Optional[int] = Query(None, description="Фильтр по складу"),
    status: Optional[DocumentStatus] = Query(None, description="Фильтр по статусу"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение списка документов"""
    query = db.query(Document).options(
        joinedload(Document.warehouse),
        joinedload(Document.creator)
    )
    
    if document_type:
        query = query.filter(Document.document_type == document_type)
    
    if warehouse_id:
        query = query.filter(Document.warehouse_id == warehouse_id)
    
    if status:
        query = query.filter(Document.status == status)
    
    documents = query.offset(skip).limit(limit).all()
    return documents


@router.get("/{document_id}", response_model=DocumentWithItems)
async def get_document(
    document_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Получение документа по ID с строками"""
    document = db.query(Document).options(
        joinedload(Document.warehouse),
        joinedload(Document.creator),
        joinedload(Document.items).joinedload(DocumentItem.nomenclature),
        joinedload(Document.items).joinedload(DocumentItem.unit)
    ).filter(Document.id == document_id).first()
    
    if not document:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Документ не найден"
        )
    return document


@router.post("/", response_model=DocumentSchema)
async def create_document(
    document_data: DocumentCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Создание нового документа"""
    # Проверяем, существует ли склад
    warehouse = db.query(Warehouse).filter(Warehouse.id == document_data.warehouse_id).first()
    if not warehouse:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Склад не найден"
        )
    
    # Проверяем уникальность номера документа
    existing_document = db.query(Document).filter(
        Document.document_number == document_data.document_number
    ).first()
    if existing_document:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Документ с таким номером уже существует"
        )
    
    # Создаем новый документ
    document = Document(
        document_type=document_data.document_type,
        document_number=document_data.document_number,
        warehouse_id=document_data.warehouse_id,
        date=document_data.date,
        status=document_data.status,
        created_by=current_user.id,
        description=document_data.description
    )
    
    db.add(document)
    db.commit()
    db.refresh(document)
    
    # Загружаем связанные данные для ответа
    document = db.query(Document).options(
        joinedload(Document.warehouse),
        joinedload(Document.creator)
    ).filter(Document.id == document.id).first()
    
    return document


@router.put("/{document_id}", response_model=DocumentSchema)
async def update_document(
    document_id: int,
    document_data: DocumentUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Обновление документа"""
    document = db.query(Document).filter(Document.id == document_id).first()
    if not document:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Документ не найден"
        )
    
    # Проверяем, можно ли редактировать документ
    if document.status == DocumentStatus.POSTED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Нельзя редактировать проведенный документ"
        )
    
    # Проверяем уникальность номера документа, если он изменился
    if document_data.document_number and document_data.document_number != document.document_number:
        existing_document = db.query(Document).filter(
            and_(
                Document.document_number == document_data.document_number,
                Document.id != document_id
            )
        ).first()
        if existing_document:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Документ с таким номером уже существует"
            )
    
    # Обновляем поля
    update_data = document_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(document, field, value)
    
    db.commit()
    db.refresh(document)
    
    # Загружаем связанные данные для ответа
    document = db.query(Document).options(
        joinedload(Document.warehouse),
        joinedload(Document.creator)
    ).filter(Document.id == document.id).first()
    
    return document


@router.delete("/{document_id}")
async def delete_document(
    document_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Удаление документа"""
    document = db.query(Document).filter(Document.id == document_id).first()
    if not document:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Документ не найден"
        )
    
    # Проверяем, можно ли удалить документ
    if document.status == DocumentStatus.POSTED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Нельзя удалить проведенный документ"
        )
    
    db.delete(document)
    db.commit()
    
    return {"message": "Документ удален"}


@router.patch("/{document_id}/post")
async def post_document(
    document_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Проведение документа"""
    document = db.query(Document).options(
        joinedload(Document.items)
    ).filter(Document.id == document_id).first()
    
    if not document:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Документ не найден"
        )
    
    if document.status == DocumentStatus.POSTED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Документ уже проведен"
        )
    
    if not document.items:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Нельзя провести документ без строк"
        )
    
    # Здесь должна быть логика проведения документа
    # (обновление остатков, создание движений и т.д.)
    # Пока просто меняем статус
    
    document.status = DocumentStatus.POSTED
    db.commit()
    
    return {"message": "Документ проведен"}


@router.patch("/{document_id}/cancel")
async def cancel_document(
    document_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Отмена документа"""
    document = db.query(Document).filter(Document.id == document_id).first()
    
    if not document:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Документ не найден"
        )
    
    if document.status == DocumentStatus.CANCELLED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Документ уже отменен"
        )
    
    # Здесь должна быть логика отмены документа
    # (обратное обновление остатков и т.д.)
    # Пока просто меняем статус
    
    document.status = DocumentStatus.CANCELLED
    db.commit()
    
    return {"message": "Документ отменен"}


# Эндпоинты для работы со строками документов
@router.post("/{document_id}/items", response_model=DocumentItemSchema)
async def create_document_item(
    document_id: int,
    item_data: DocumentItemCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Создание строки документа"""
    # Проверяем, существует ли документ
    document = db.query(Document).filter(Document.id == document_id).first()
    if not document:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Документ не найден"
        )
    
    # Проверяем, можно ли редактировать документ
    if document.status == DocumentStatus.POSTED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Нельзя редактировать проведенный документ"
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
    
    # Создаем новую строку документа
    item = DocumentItem(
        document_id=document_id,
        nomenclature_id=item_data.nomenclature_id,
        quantity=item_data.quantity,
        unit_id=item_data.unit_id,
        price=item_data.price,
        total=item_data.total,
        description=item_data.description
    )
    
    db.add(item)
    db.commit()
    db.refresh(item)
    
    # Загружаем связанные данные для ответа
    item = db.query(DocumentItem).options(
        joinedload(DocumentItem.nomenclature),
        joinedload(DocumentItem.unit)
    ).filter(DocumentItem.id == item.id).first()
    
    return item


@router.put("/items/{item_id}", response_model=DocumentItemSchema)
async def update_document_item(
    item_id: int,
    item_data: DocumentItemUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Обновление строки документа"""
    item = db.query(DocumentItem).filter(DocumentItem.id == item_id).first()
    if not item:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Строка документа не найдена"
        )
    
    # Проверяем, можно ли редактировать документ
    if item.document.status == DocumentStatus.POSTED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Нельзя редактировать проведенный документ"
        )
    
    # Обновляем поля
    update_data = item_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(item, field, value)
    
    db.commit()
    db.refresh(item)
    
    # Загружаем связанные данные для ответа
    item = db.query(DocumentItem).options(
        joinedload(DocumentItem.nomenclature),
        joinedload(DocumentItem.unit)
    ).filter(DocumentItem.id == item.id).first()
    
    return item


@router.delete("/items/{item_id}")
async def delete_document_item(
    item_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user_from_token)
):
    """Удаление строки документа"""
    item = db.query(DocumentItem).filter(DocumentItem.id == item_id).first()
    if not item:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Строка документа не найдена"
        )
    
    # Проверяем, можно ли редактировать документ
    if item.document.status == DocumentStatus.POSTED:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Нельзя редактировать проведенный документ"
        )
    
    db.delete(item)
    db.commit()
    
    return {"message": "Строка документа удалена"}

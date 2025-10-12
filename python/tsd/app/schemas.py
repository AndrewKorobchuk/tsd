import json
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from decimal import Decimal
from app.models import DocumentType, DocumentStatus, MovementType, InventoryStatus


class UserBase(BaseModel):
    username: str
    email: str


class UserCreate(UserBase):
    password: str


class UserLogin(BaseModel):
    username: str
    password: str


class User(UserBase):
    id: int
    is_active: bool
    created_at: datetime
    updated_at: Optional[datetime] = None

    class Config:
        from_attributes = True


# OAuth Schemas
class OAuthClientCreate(BaseModel):
    client_name: str
    redirect_uris: List[str]
    scope: str = "read write"


class OAuthClient(OAuthClientCreate):
    id: int
    client_id: str
    is_active: bool
    created_at: datetime

    class Config:
        from_attributes = True
    
    @classmethod
    def from_orm(cls, obj):
        # Преобразуем JSON строку в список для redirect_uris
        data = {
            "id": obj.id,
            "client_id": obj.client_id,
            "client_name": obj.client_name,
            "redirect_uris": json.loads(obj.redirect_uris) if obj.redirect_uris else [],
            "scope": obj.scope,
            "is_active": obj.is_active,
            "created_at": obj.created_at
        }
        return cls(**data)


class OAuthClientResponse(OAuthClient):
    client_secret: str  # Только для ответа при регистрации


class TokenRequest(BaseModel):
    grant_type: str
    client_id: str
    client_secret: Optional[str] = None
    username: Optional[str] = None
    password: Optional[str] = None
    scope: Optional[str] = "read write"


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "Bearer"
    expires_in: int
    refresh_token: Optional[str] = None
    scope: str


class TokenData(BaseModel):
    username: Optional[str] = None
    client_id: Optional[str] = None
    scope: Optional[str] = None


# Unit of Measure Schemas
class UnitOfMeasureBase(BaseModel):
    code: str
    name: str
    short_name: str
    description: Optional[str] = None
    is_active: bool = True


class UnitOfMeasureCreate(UnitOfMeasureBase):
    pass


class UnitOfMeasureUpdate(BaseModel):
    code: Optional[str] = None
    name: Optional[str] = None
    short_name: Optional[str] = None
    description: Optional[str] = None
    is_active: Optional[bool] = None


class UnitOfMeasure(UnitOfMeasureBase):
    id: int
    created_at: datetime
    updated_at: Optional[datetime] = None

    class Config:
        from_attributes = True


# Nomenclature Category Schemas
class NomenclatureCategoryBase(BaseModel):
    code: str
    name: str
    description: Optional[str] = None
    is_active: bool = True


class NomenclatureCategoryCreate(NomenclatureCategoryBase):
    pass


class NomenclatureCategoryUpdate(BaseModel):
    code: Optional[str] = None
    name: Optional[str] = None
    description: Optional[str] = None
    is_active: Optional[bool] = None


class NomenclatureCategory(NomenclatureCategoryBase):
    id: int
    created_at: datetime
    updated_at: Optional[datetime] = None

    class Config:
        from_attributes = True


# Nomenclature Schemas
class NomenclatureBase(BaseModel):
    code: str
    category_id: int
    name: str
    base_unit_id: int
    description_ru: Optional[str] = None
    description_ua: Optional[str] = None
    is_active: bool = True


class NomenclatureCreate(NomenclatureBase):
    pass


class NomenclatureUpdate(BaseModel):
    code: Optional[str] = None
    category_id: Optional[int] = None
    name: Optional[str] = None
    base_unit_id: Optional[int] = None
    description_ru: Optional[str] = None
    description_ua: Optional[str] = None
    is_active: Optional[bool] = None


class Nomenclature(NomenclatureBase):
    id: int
    created_at: datetime
    updated_at: Optional[datetime] = None
    category: Optional[NomenclatureCategory] = None
    base_unit: Optional[UnitOfMeasure] = None

    class Config:
        from_attributes = True


# Warehouse Schemas
class WarehouseBase(BaseModel):
    code: str
    name: str
    address: Optional[str] = None
    description: Optional[str] = None
    is_active: bool = True


class WarehouseCreate(WarehouseBase):
    pass


class WarehouseUpdate(BaseModel):
    code: Optional[str] = None
    name: Optional[str] = None
    address: Optional[str] = None
    description: Optional[str] = None
    is_active: Optional[bool] = None


class Warehouse(WarehouseBase):
    id: int
    created_at: datetime
    updated_at: Optional[datetime] = None

    class Config:
        from_attributes = True


# Stock Schemas
class StockBase(BaseModel):
    nomenclature_id: int
    warehouse_id: int
    quantity: Decimal
    reserved_quantity: Decimal = Decimal('0')


class StockCreate(StockBase):
    pass


class StockUpdate(BaseModel):
    quantity: Optional[Decimal] = None
    reserved_quantity: Optional[Decimal] = None


class Stock(StockBase):
    id: int
    last_updated: datetime
    created_at: datetime
    nomenclature: Optional[Nomenclature] = None
    warehouse: Optional[Warehouse] = None

    class Config:
        from_attributes = True


# Document Schemas
class DocumentBase(BaseModel):
    document_type: DocumentType
    document_number: str
    warehouse_id: int
    date: datetime
    status: DocumentStatus = DocumentStatus.DRAFT
    description: Optional[str] = None


class DocumentCreate(DocumentBase):
    pass


class DocumentUpdate(BaseModel):
    document_type: Optional[DocumentType] = None
    document_number: Optional[str] = None
    warehouse_id: Optional[int] = None
    date: Optional[datetime] = None
    status: Optional[DocumentStatus] = None
    description: Optional[str] = None


class Document(DocumentBase):
    id: int
    created_by: int
    created_at: datetime
    updated_at: Optional[datetime] = None
    warehouse: Optional[Warehouse] = None
    creator: Optional[User] = None
    items: List['DocumentItem'] = []

    class Config:
        from_attributes = True


# DocumentItem Schemas
class DocumentItemBase(BaseModel):
    nomenclature_id: int
    quantity: Decimal
    unit_id: int
    price: Optional[Decimal] = None
    total: Optional[Decimal] = None
    description: Optional[str] = None


class DocumentItemCreate(DocumentItemBase):
    pass


class DocumentItemUpdate(BaseModel):
    nomenclature_id: Optional[int] = None
    quantity: Optional[Decimal] = None
    unit_id: Optional[int] = None
    price: Optional[Decimal] = None
    total: Optional[Decimal] = None
    description: Optional[str] = None


class DocumentItem(DocumentItemBase):
    id: int
    document_id: int
    created_at: datetime
    nomenclature: Optional[Nomenclature] = None
    unit: Optional[UnitOfMeasure] = None

    class Config:
        from_attributes = True


# Inventory Schemas
class InventoryBase(BaseModel):
    warehouse_id: int
    inventory_number: str
    date_start: datetime
    date_end: Optional[datetime] = None
    status: InventoryStatus = InventoryStatus.IN_PROGRESS
    description: Optional[str] = None


class InventoryCreate(InventoryBase):
    pass


class InventoryUpdate(BaseModel):
    warehouse_id: Optional[int] = None
    inventory_number: Optional[str] = None
    date_start: Optional[datetime] = None
    date_end: Optional[datetime] = None
    status: Optional[InventoryStatus] = None
    description: Optional[str] = None


class Inventory(InventoryBase):
    id: int
    created_by: int
    created_at: datetime
    updated_at: Optional[datetime] = None
    warehouse: Optional[Warehouse] = None
    creator: Optional[User] = None
    items: List['InventoryItem'] = []

    class Config:
        from_attributes = True


# InventoryItem Schemas
class InventoryItemBase(BaseModel):
    nomenclature_id: int
    planned_quantity: Decimal
    actual_quantity: Optional[Decimal] = None
    difference: Optional[Decimal] = None
    unit_id: int


class InventoryItemCreate(InventoryItemBase):
    pass


class InventoryItemUpdate(BaseModel):
    nomenclature_id: Optional[int] = None
    planned_quantity: Optional[Decimal] = None
    actual_quantity: Optional[Decimal] = None
    difference: Optional[Decimal] = None
    unit_id: Optional[int] = None


class InventoryItem(InventoryItemBase):
    id: int
    inventory_id: int
    counted_by: Optional[int] = None
    counted_at: Optional[datetime] = None
    created_at: datetime
    updated_at: Optional[datetime] = None
    nomenclature: Optional[Nomenclature] = None
    unit: Optional[UnitOfMeasure] = None
    counter: Optional[User] = None

    class Config:
        from_attributes = True


# StockMovement Schemas
class StockMovementBase(BaseModel):
    nomenclature_id: int
    warehouse_id: int
    movement_type: MovementType
    quantity: Decimal
    document_id: Optional[int] = None
    inventory_id: Optional[int] = None
    date: datetime
    description: Optional[str] = None


class StockMovementCreate(StockMovementBase):
    pass


class StockMovement(StockMovementBase):
    id: int
    user_id: int
    created_at: datetime
    nomenclature: Optional[Nomenclature] = None
    warehouse: Optional[Warehouse] = None
    document: Optional[Document] = None
    inventory: Optional[Inventory] = None
    user: Optional[User] = None

    class Config:
        from_attributes = True


# Дополнительные схемы для API
class DocumentWithItems(Document):
    items: List[DocumentItem] = []


class InventoryWithItems(Inventory):
    items: List[InventoryItem] = []


class StockSummary(BaseModel):
    nomenclature_id: int
    nomenclature_code: str
    nomenclature_name: str
    warehouse_id: int
    warehouse_code: str
    warehouse_name: str
    quantity: Decimal
    reserved_quantity: Decimal
    available_quantity: Decimal
    last_updated: datetime

    class Config:
        from_attributes = True


# Barcode Schemas
class BarcodeBase(BaseModel):
    nomenclature_id: int
    barcode: str
    barcode_type: str = "EAN13"
    is_primary: bool = False
    is_active: bool = True
    description: Optional[str] = None


class BarcodeCreate(BarcodeBase):
    pass


class BarcodeUpdate(BaseModel):
    nomenclature_id: Optional[int] = None
    barcode: Optional[str] = None
    barcode_type: Optional[str] = None
    is_primary: Optional[bool] = None
    is_active: Optional[bool] = None
    description: Optional[str] = None


class Barcode(BarcodeBase):
    id: int
    created_at: datetime
    updated_at: Optional[datetime] = None
    nomenclature: Optional[Nomenclature] = None

    class Config:
        from_attributes = True


# Обновляем ссылки на модели
Document.model_rebuild()
DocumentItem.model_rebuild()
Inventory.model_rebuild()
InventoryItem.model_rebuild()
StockMovement.model_rebuild()

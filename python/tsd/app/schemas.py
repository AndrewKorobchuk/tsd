import json
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime


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

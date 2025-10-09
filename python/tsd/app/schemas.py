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

from sqlalchemy import Column, Integer, String, DateTime, Boolean, Text, ForeignKey
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.database import Base


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True, nullable=False)
    email = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())


class OAuthClient(Base):
    __tablename__ = "oauth_clients"

    id = Column(Integer, primary_key=True, index=True)
    client_id = Column(String, unique=True, index=True, nullable=False)
    client_secret = Column(String, nullable=False)
    client_name = Column(String, nullable=False)
    redirect_uris = Column(Text)  # JSON string of allowed redirect URIs
    scope = Column(String, default="read write")
    is_confidential = Column(Boolean, default=True)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())


class OAuthToken(Base):
    __tablename__ = "oauth_tokens"

    id = Column(Integer, primary_key=True, index=True)
    client_id = Column(String, ForeignKey("oauth_clients.client_id"), nullable=False)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    token_type = Column(String, nullable=False)  # 'access_token' or 'refresh_token'
    access_token = Column(String, unique=True, index=True, nullable=True)
    refresh_token = Column(String, unique=True, index=True, nullable=True)
    scope = Column(String, nullable=False)
    expires_at = Column(DateTime(timezone=True), nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    # Relationships
    client = relationship("OAuthClient", backref="tokens")
    user = relationship("User", backref="tokens")


class UnitOfMeasure(Base):
    __tablename__ = "units_of_measure"

    id = Column(Integer, primary_key=True, index=True)
    code = Column(String, unique=True, index=True, nullable=False)  # Код единицы измерения
    name = Column(String, nullable=False)  # Название единицы измерения
    short_name = Column(String, nullable=False)  # Краткое название
    description = Column(Text, nullable=True)  # Описание
    is_active = Column(Boolean, default=True)  # Активна ли единица измерения
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())


class NomenclatureCategory(Base):
    __tablename__ = "nomenclature_categories"

    id = Column(Integer, primary_key=True, index=True)
    code = Column(String, unique=True, index=True, nullable=False)  # Код категории
    name = Column(String, nullable=False)  # Название категории
    description = Column(Text, nullable=True)  # Описание категории
    is_active = Column(Boolean, default=True)  # Активна ли категория
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())


class Nomenclature(Base):
    __tablename__ = "nomenclature"

    id = Column(Integer, primary_key=True, index=True)
    code = Column(String, unique=True, index=True, nullable=False)  # Код номенклатуры
    category_id = Column(Integer, ForeignKey("nomenclature_categories.id"), nullable=False)  # ID категории
    name = Column(String, nullable=False)  # Наименование
    base_unit_id = Column(Integer, ForeignKey("units_of_measure.id"), nullable=False)  # ID базовой единицы измерения
    description_ru = Column(Text, nullable=True)  # Описание на русском
    description_ua = Column(Text, nullable=True)  # Описание на украинском
    is_active = Column(Boolean, default=True)  # Признак активности
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Relationships
    category = relationship("NomenclatureCategory", backref="nomenclature_items")
    base_unit = relationship("UnitOfMeasure", backref="nomenclature_items")

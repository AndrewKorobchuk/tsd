from sqlalchemy import Column, Integer, String, DateTime, Boolean, Text, ForeignKey, Numeric, Enum
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.database import Base
import enum


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


class Warehouse(Base):
    __tablename__ = "warehouses"

    id = Column(Integer, primary_key=True, index=True)
    code = Column(String, unique=True, index=True, nullable=False)  # Код склада
    name = Column(String, nullable=False)  # Название склада
    address = Column(Text, nullable=True)  # Адрес склада
    description = Column(Text, nullable=True)  # Описание склада
    is_active = Column(Boolean, default=True)  # Признак активности
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())


# Enums для документов
class DocumentType(str, enum.Enum):
    RECEIPT = "receipt"  # Приход
    EXPENSE = "expense"  # Расход
    TRANSFER = "transfer"  # Перемещение
    INVENTORY = "inventory"  # Инвентаризация


class DocumentStatus(str, enum.Enum):
    DRAFT = "draft"  # Черновик
    POSTED = "posted"  # Проведен
    CANCELLED = "cancelled"  # Отменен


class MovementType(str, enum.Enum):
    RECEIPT = "receipt"  # Приход
    EXPENSE = "expense"  # Расход
    TRANSFER_IN = "transfer_in"  # Перемещение приход
    TRANSFER_OUT = "transfer_out"  # Перемещение расход
    INVENTORY = "inventory"  # Инвентаризация


class InventoryStatus(str, enum.Enum):
    IN_PROGRESS = "in_progress"  # В процессе
    COMPLETED = "completed"  # Завершена
    CANCELLED = "cancelled"  # Отменена


# Остатки товаров на складах
class Stock(Base):
    __tablename__ = "stocks"

    id = Column(Integer, primary_key=True, index=True)
    nomenclature_id = Column(Integer, ForeignKey("nomenclature.id"), nullable=False)
    warehouse_id = Column(Integer, ForeignKey("warehouses.id"), nullable=False)
    quantity = Column(Numeric(15, 3), default=0, nullable=False)  # Текущее количество
    reserved_quantity = Column(Numeric(15, 3), default=0, nullable=False)  # Зарезервированное количество
    last_updated = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    # Relationships
    nomenclature = relationship("Nomenclature", backref="stocks")
    warehouse = relationship("Warehouse", backref="stocks")
    
    # Уникальный индекс по номенклатуре и складу
    __table_args__ = (
        {"extend_existing": True}
    )


# Документы движения товаров
class Document(Base):
    __tablename__ = "documents"

    id = Column(Integer, primary_key=True, index=True)
    document_type = Column(Enum(DocumentType), nullable=False)  # Тип документа
    document_number = Column(String, nullable=False)  # Номер документа
    warehouse_id = Column(Integer, ForeignKey("warehouses.id"), nullable=False)  # Склад
    date = Column(DateTime(timezone=True), nullable=False)  # Дата документа
    status = Column(Enum(DocumentStatus), default=DocumentStatus.DRAFT, nullable=False)  # Статус
    created_by = Column(Integer, ForeignKey("users.id"), nullable=False)  # Создатель
    description = Column(Text, nullable=True)  # Описание документа
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Relationships
    warehouse = relationship("Warehouse", backref="documents")
    creator = relationship("User", backref="created_documents")
    items = relationship("DocumentItem", back_populates="document", cascade="all, delete-orphan")


# Строки документов
class DocumentItem(Base):
    __tablename__ = "document_items"

    id = Column(Integer, primary_key=True, index=True)
    document_id = Column(Integer, ForeignKey("documents.id"), nullable=False)
    nomenclature_id = Column(Integer, ForeignKey("nomenclature.id"), nullable=False)
    quantity = Column(Numeric(15, 3), nullable=False)  # Количество
    unit_id = Column(Integer, ForeignKey("units_of_measure.id"), nullable=False)  # Единица измерения
    price = Column(Numeric(15, 2), nullable=True)  # Цена за единицу
    total = Column(Numeric(15, 2), nullable=True)  # Общая сумма
    description = Column(Text, nullable=True)  # Описание строки
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    # Relationships
    document = relationship("Document", back_populates="items")
    nomenclature = relationship("Nomenclature", backref="document_items")
    unit = relationship("UnitOfMeasure", backref="document_items")


# Инвентаризация
class Inventory(Base):
    __tablename__ = "inventories"

    id = Column(Integer, primary_key=True, index=True)
    warehouse_id = Column(Integer, ForeignKey("warehouses.id"), nullable=False)
    inventory_number = Column(String, nullable=False)  # Номер инвентаризации
    date_start = Column(DateTime(timezone=True), nullable=False)  # Дата начала
    date_end = Column(DateTime(timezone=True), nullable=True)  # Дата окончания
    status = Column(Enum(InventoryStatus), default=InventoryStatus.IN_PROGRESS, nullable=False)
    created_by = Column(Integer, ForeignKey("users.id"), nullable=False)
    description = Column(Text, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Relationships
    warehouse = relationship("Warehouse", backref="inventories")
    creator = relationship("User", backref="created_inventories")
    items = relationship("InventoryItem", back_populates="inventory", cascade="all, delete-orphan")


# Строки инвентаризации
class InventoryItem(Base):
    __tablename__ = "inventory_items"

    id = Column(Integer, primary_key=True, index=True)
    inventory_id = Column(Integer, ForeignKey("inventories.id"), nullable=False)
    nomenclature_id = Column(Integer, ForeignKey("nomenclature.id"), nullable=False)
    planned_quantity = Column(Numeric(15, 3), nullable=False)  # Плановое количество
    actual_quantity = Column(Numeric(15, 3), nullable=True)  # Фактическое количество
    difference = Column(Numeric(15, 3), nullable=True)  # Разница
    unit_id = Column(Integer, ForeignKey("units_of_measure.id"), nullable=False)
    counted_by = Column(Integer, ForeignKey("users.id"), nullable=True)  # Кто считал
    counted_at = Column(DateTime(timezone=True), nullable=True)  # Когда считал
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Relationships
    inventory = relationship("Inventory", back_populates="items")
    nomenclature = relationship("Nomenclature", backref="inventory_items")
    unit = relationship("UnitOfMeasure", backref="inventory_items")
    counter = relationship("User", backref="counted_items")


# История движений товаров
class StockMovement(Base):
    __tablename__ = "stock_movements"

    id = Column(Integer, primary_key=True, index=True)
    nomenclature_id = Column(Integer, ForeignKey("nomenclature.id"), nullable=False)
    warehouse_id = Column(Integer, ForeignKey("warehouses.id"), nullable=False)
    movement_type = Column(Enum(MovementType), nullable=False)  # Тип движения
    quantity = Column(Numeric(15, 3), nullable=False)  # Количество
    document_id = Column(Integer, ForeignKey("documents.id"), nullable=True)  # Ссылка на документ
    inventory_id = Column(Integer, ForeignKey("inventories.id"), nullable=True)  # Ссылка на инвентаризацию
    date = Column(DateTime(timezone=True), nullable=False)  # Дата движения
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)  # Пользователь
    description = Column(Text, nullable=True)  # Описание движения
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    # Relationships
    nomenclature = relationship("Nomenclature", backref="stock_movements")
    warehouse = relationship("Warehouse", backref="stock_movements")
    document = relationship("Document", backref="stock_movements")
    inventory = relationship("Inventory", backref="stock_movements")
    user = relationship("User", backref="stock_movements")


# Штрих-коды номенклатуры
class Barcode(Base):
    __tablename__ = "barcodes"

    id = Column(Integer, primary_key=True, index=True)
    nomenclature_id = Column(Integer, ForeignKey("nomenclature.id"), nullable=False)
    barcode = Column(String, unique=True, index=True, nullable=False)  # Штрих-код
    barcode_type = Column(String, nullable=False, default="EAN13")  # Тип штрих-кода (EAN13, Code128, QR и т.д.)
    is_primary = Column(Boolean, default=False, nullable=False)  # Основной штрих-код
    is_active = Column(Boolean, default=True, nullable=False)  # Активен ли штрих-код
    description = Column(Text, nullable=True)  # Описание штрих-кода
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Relationships
    nomenclature = relationship("Nomenclature", backref="barcodes")

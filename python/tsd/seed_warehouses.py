#!/usr/bin/env python3
"""
Скрипт для добавления тестовых данных складов
"""

from sqlalchemy.orm import Session
from sqlalchemy import create_engine
from app.database import SessionLocal
from app.models import Base, Warehouse
from app.config import settings

# Создаем движок с правильным URL
engine = create_engine(settings.database_url)

# Создаем таблицы
Base.metadata.create_all(bind=engine)

# Тестовые данные складов
WAREHOUSES_DATA = [
    {
        "code": "MAIN_WH",
        "name": "Главный склад",
        "address": "ул. Промышленная, 15, Киев, Украина",
        "description": "Основной склад для хранения товаров"
    },
    {
        "code": "RETAIL_WH",
        "name": "Розничный склад",
        "address": "пр. Победы, 42, Киев, Украина",
        "description": "Склад для розничной торговли"
    },
    {
        "code": "COLD_WH",
        "name": "Холодильный склад",
        "address": "ул. Холодильная, 8, Киев, Украина",
        "description": "Склад для хранения скоропортящихся товаров"
    },
    {
        "code": "DRY_WH",
        "name": "Сухой склад",
        "address": "ул. Складская, 25, Киев, Украина",
        "description": "Склад для хранения сухих товаров"
    },
    {
        "code": "RETURN_WH",
        "name": "Склад возвратов",
        "address": "ул. Возвратная, 3, Киев, Украина",
        "description": "Склад для товаров, возвращенных покупателями"
    },
    {
        "code": "QUARANTINE_WH",
        "name": "Карантинный склад",
        "address": "ул. Карантинная, 12, Киев, Украина",
        "description": "Склад для товаров на карантине"
    },
    {
        "code": "EXPRESS_WH",
        "name": "Экспресс склад",
        "address": "ул. Быстрая, 7, Киев, Украина",
        "description": "Склад для быстрой отгрузки"
    },
    {
        "code": "SEASONAL_WH",
        "name": "Сезонный склад",
        "address": "ул. Сезонная, 18, Киев, Украина",
        "description": "Склад для сезонных товаров"
    }
]


def seed_data():
    db: Session = SessionLocal()
    try:
        # Добавляем склады
        print("Добавляем склады...")
        for warehouse_data in WAREHOUSES_DATA:
            existing_warehouse = db.query(Warehouse).filter(Warehouse.code == warehouse_data["code"]).first()
            if not existing_warehouse:
                db_warehouse = Warehouse(**warehouse_data)
                db.add(db_warehouse)
                db.commit()
                db.refresh(db_warehouse)
                print(f"- Добавлен склад: {db_warehouse.name} ({db_warehouse.code})")
            else:
                print(f"- Склад уже существует: {existing_warehouse.name} ({existing_warehouse.code})")
        print(f"Добавлено {len(WAREHOUSES_DATA)} складов")

    except Exception as e:
        print(f"Ошибка при добавлении тестовых данных: {e}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    seed_data()

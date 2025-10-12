#!/usr/bin/env python3
"""
Скрипт для добавления тестовых данных единиц измерения
"""

from sqlalchemy.orm import Session
from sqlalchemy import create_engine
from app.database import SessionLocal
from app.models import Base, UnitOfMeasure
from app.config import settings

# Создаем движок с правильным URL
engine = create_engine(settings.database_url)

# Создаем таблицы
Base.metadata.create_all(bind=engine)

# Тестовые данные единиц измерения
UNITS_DATA = [
    {
        "code": "kg",
        "name": "Килограмм",
        "short_name": "кг",
        "description": "Основная единица измерения массы в системе СИ"
    },
    {
        "code": "g",
        "name": "Грамм",
        "short_name": "г",
        "description": "Единица измерения массы, равная 1/1000 килограмма"
    },
    {
        "code": "t",
        "name": "Тонна",
        "short_name": "т",
        "description": "Единица измерения массы, равная 1000 килограммам"
    },
    {
        "code": "l",
        "name": "Литр",
        "short_name": "л",
        "description": "Единица измерения объема"
    },
    {
        "code": "ml",
        "name": "Миллилитр",
        "short_name": "мл",
        "description": "Единица измерения объема, равная 1/1000 литра"
    },
    {
        "code": "m",
        "name": "Метр",
        "short_name": "м",
        "description": "Основная единица измерения длины в системе СИ"
    },
    {
        "code": "cm",
        "name": "Сантиметр",
        "short_name": "см",
        "description": "Единица измерения длины, равная 1/100 метра"
    },
    {
        "code": "mm",
        "name": "Миллиметр",
        "short_name": "мм",
        "description": "Единица измерения длины, равная 1/1000 метра"
    },
    {
        "code": "m2",
        "name": "Квадратный метр",
        "short_name": "м²",
        "description": "Единица измерения площади"
    },
    {
        "code": "m3",
        "name": "Кубический метр",
        "short_name": "м³",
        "description": "Единица измерения объема"
    },
    {
        "code": "pcs",
        "name": "Штука",
        "short_name": "шт",
        "description": "Единица измерения количества"
    },
    {
        "code": "box",
        "name": "Коробка",
        "short_name": "кор",
        "description": "Единица измерения упаковки"
    },
    {
        "code": "pack",
        "name": "Упаковка",
        "short_name": "уп",
        "description": "Единица измерения упаковки товара"
    },
    {
        "code": "set",
        "name": "Комплект",
        "short_name": "компл",
        "description": "Единица измерения набора товаров"
    },
    {
        "code": "pair",
        "name": "Пара",
        "short_name": "пар",
        "description": "Единица измерения пары предметов"
    },
    {
        "code": "dozen",
        "name": "Дюжина",
        "short_name": "дюж",
        "description": "Единица измерения, равная 12 штукам"
    },
    {
        "code": "hour",
        "name": "Час",
        "short_name": "ч",
        "description": "Единица измерения времени"
    },
    {
        "code": "day",
        "name": "День",
        "short_name": "дн",
        "description": "Единица измерения времени"
    },
    {
        "code": "month",
        "name": "Месяц",
        "short_name": "мес",
        "description": "Единица измерения времени"
    },
    {
        "code": "year",
        "name": "Год",
        "short_name": "г",
        "description": "Единица измерения времени"
    }
]


def seed_units():
    """Добавление тестовых данных единиц измерения"""
    db: Session = SessionLocal()
    
    try:
        # Проверяем, есть ли уже данные
        existing_count = db.query(UnitOfMeasure).count()
        if existing_count > 0:
            print(f"В базе данных уже есть {existing_count} единиц измерения")
            return
        
        # Добавляем единицы измерения
        for unit_data in UNITS_DATA:
            unit = UnitOfMeasure(**unit_data)
            db.add(unit)
        
        db.commit()
        print(f"Добавлено {len(UNITS_DATA)} единиц измерения")
        
        # Выводим список добавленных единиц
        print("\nДобавленные единицы измерения:")
        for unit in db.query(UnitOfMeasure).all():
            print(f"- {unit.code}: {unit.name} ({unit.short_name})")
            
    except Exception as e:
        print(f"Ошибка при добавлении данных: {e}")
        db.rollback()
    finally:
        db.close()


if __name__ == "__main__":
    seed_units()

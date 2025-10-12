#!/usr/bin/env python3
"""
Скрипт для добавления тестовых данных категорий номенклатуры и номенклатуры
"""

from sqlalchemy.orm import Session
from sqlalchemy import create_engine
from app.database import SessionLocal
from app.models import Base, NomenclatureCategory, Nomenclature, UnitOfMeasure
from app.config import settings

# Создаем движок с правильным URL
engine = create_engine(settings.database_url)

# Создаем таблицы
Base.metadata.create_all(bind=engine)

# Тестовые данные категорий номенклатуры
CATEGORIES_DATA = [
    {
        "code": "FOOD",
        "name": "Продукты питания",
        "description": "Категория продуктов питания и напитков"
    },
    {
        "code": "CLOTHES",
        "name": "Одежда",
        "description": "Категория одежды и обуви"
    },
    {
        "code": "ELECTRONICS",
        "name": "Электроника",
        "description": "Категория электронных товаров"
    },
    {
        "code": "BOOKS",
        "name": "Книги",
        "description": "Категория книг и печатной продукции"
    },
    {
        "code": "HOUSEHOLD",
        "name": "Товары для дома",
        "description": "Категория товаров для дома и быта"
    }
]

# Тестовые данные номенклатуры
NOMENCLATURE_DATA = [
    {
        "code": "BREAD_001",
        "category_code": "FOOD",
        "name": "Хлеб белый",
        "base_unit_code": "pcs",
        "description_ru": "Свежий белый хлеб",
        "description_ua": "Свіжий білий хліб"
    },
    {
        "code": "MILK_001",
        "category_code": "FOOD",
        "name": "Молоко 3.2%",
        "base_unit_code": "l",
        "description_ru": "Пастеризованное молоко жирностью 3.2%",
        "description_ua": "Пастеризоване молоко жирністю 3.2%"
    },
    {
        "code": "SHIRT_001",
        "category_code": "CLOTHES",
        "name": "Рубашка мужская",
        "base_unit_code": "pcs",
        "description_ru": "Мужская рубашка из хлопка",
        "description_ua": "Чоловіча сорочка з бавовни"
    },
    {
        "code": "PHONE_001",
        "category_code": "ELECTRONICS",
        "name": "Смартфон",
        "base_unit_code": "pcs",
        "description_ru": "Смартфон с Android",
        "description_ua": "Смартфон з Android"
    },
    {
        "code": "BOOK_001",
        "category_code": "BOOKS",
        "name": "Учебник математики",
        "base_unit_code": "pcs",
        "description_ru": "Учебник математики для 5 класса",
        "description_ua": "Підручник математики для 5 класу"
    },
    {
        "code": "SOAP_001",
        "category_code": "HOUSEHOLD",
        "name": "Мыло туалетное",
        "base_unit_code": "pcs",
        "description_ru": "Туалетное мыло 100г",
        "description_ua": "Туалетне мило 100г"
    }
]


def seed_nomenclature():
    """Добавление тестовых данных категорий номенклатуры и номенклатуры"""
    db: Session = SessionLocal()
    
    try:
        # Проверяем, есть ли уже данные
        existing_categories = db.query(NomenclatureCategory).count()
        existing_nomenclature = db.query(Nomenclature).count()
        
        if existing_categories > 0 or existing_nomenclature > 0:
            print(f"В базе данных уже есть {existing_categories} категорий и {existing_nomenclature} номенклатуры")
            return
        
        # Создаем категории
        categories_map = {}
        for category_data in CATEGORIES_DATA:
            category = NomenclatureCategory(**category_data)
            db.add(category)
            db.flush()  # Получаем ID без коммита
            categories_map[category_data["code"]] = category.id
        
        # Получаем единицы измерения
        units_map = {}
        units = db.query(UnitOfMeasure).all()
        for unit in units:
            units_map[unit.code] = unit.id
        
        # Создаем номенклатуру
        for nomenclature_data in NOMENCLATURE_DATA:
            # Получаем ID категории и единицы измерения
            category_id = categories_map.get(nomenclature_data["category_code"])
            unit_id = units_map.get(nomenclature_data["base_unit_code"])
            
            if not category_id:
                print(f"Категория с кодом {nomenclature_data['category_code']} не найдена")
                continue
                
            if not unit_id:
                print(f"Единица измерения с кодом {nomenclature_data['base_unit_code']} не найдена")
                continue
            
            nomenclature = Nomenclature(
                code=nomenclature_data["code"],
                category_id=category_id,
                name=nomenclature_data["name"],
                base_unit_id=unit_id,
                description_ru=nomenclature_data["description_ru"],
                description_ua=nomenclature_data["description_ua"]
            )
            db.add(nomenclature)
        
        db.commit()
        print(f"Добавлено {len(CATEGORIES_DATA)} категорий номенклатуры")
        print(f"Добавлено {len(NOMENCLATURE_DATA)} единиц номенклатуры")
        
        # Выводим список добавленных категорий
        print("\nДобавленные категории номенклатуры:")
        for category in db.query(NomenclatureCategory).all():
            print(f"- {category.code}: {category.name}")
        
        # Выводим список добавленной номенклатуры
        print("\nДобавленная номенклатура:")
        for nomenclature in db.query(Nomenclature).all():
            print(f"- {nomenclature.code}: {nomenclature.name}")
            
    except Exception as e:
        print(f"Ошибка при добавлении данных: {e}")
        db.rollback()
    finally:
        db.close()


if __name__ == "__main__":
    seed_nomenclature()

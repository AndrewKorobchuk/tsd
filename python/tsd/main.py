from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.database import engine
from app.models import Base
from app.routers import oauth, units, nomenclature_categories, nomenclature, warehouses, stocks, documents, inventories, barcodes

# Создание таблиц в базе данных
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="TSD API",
    description="API для мобильного приложения TSD с OAuth 2.0 авторизацией",
    version="1.0.0"
)

# Настройка CORS для мобильного приложения
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # В продакшене указать конкретные домены
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Подключение роутеров
app.include_router(oauth.router, prefix="/api/v1")
app.include_router(units.router, prefix="/api/v1")
app.include_router(nomenclature_categories.router, prefix="/api/v1")
app.include_router(nomenclature.router, prefix="/api/v1")
app.include_router(warehouses.router, prefix="/api/v1")
app.include_router(stocks.router, prefix="/api/v1")
app.include_router(documents.router, prefix="/api/v1")
app.include_router(inventories.router, prefix="/api/v1")
app.include_router(barcodes.router, prefix="/api/v1")


@app.get("/")
async def root():
    """Корневой эндпоинт"""
    return {
        "message": "TSD API Server", 
        "version": "1.0.0",
        "oauth_endpoints": {
            "register_client": "/api/v1/oauth/register",
            "get_token": "/api/v1/oauth/token",
            "register_user": "/api/v1/oauth/register-user",
            "user_info": "/api/v1/oauth/me",
            "client_info": "/api/v1/oauth/client-info"
        },
        "units_endpoints": {
            "list_units": "/api/v1/units/",
            "get_unit": "/api/v1/units/{id}",
            "get_unit_by_code": "/api/v1/units/code/{code}",
            "create_unit": "/api/v1/units/",
            "update_unit": "/api/v1/units/{id}",
            "delete_unit": "/api/v1/units/{id}",
            "activate_unit": "/api/v1/units/{id}/activate"
        },
        "nomenclature_categories_endpoints": {
            "list_categories": "/api/v1/nomenclature-categories/",
            "get_category": "/api/v1/nomenclature-categories/{id}",
            "get_category_by_code": "/api/v1/nomenclature-categories/code/{code}",
            "create_category": "/api/v1/nomenclature-categories/",
            "update_category": "/api/v1/nomenclature-categories/{id}",
            "delete_category": "/api/v1/nomenclature-categories/{id}",
            "activate_category": "/api/v1/nomenclature-categories/{id}/activate"
        },
            "nomenclature_endpoints": {
                "list_nomenclature": "/api/v1/nomenclature/",
                "get_nomenclature": "/api/v1/nomenclature/{id}",
                "get_nomenclature_by_code": "/api/v1/nomenclature/code/{code}",
                "create_nomenclature": "/api/v1/nomenclature/",
                "update_nomenclature": "/api/v1/nomenclature/{id}",
                "delete_nomenclature": "/api/v1/nomenclature/{id}",
                "activate_nomenclature": "/api/v1/nomenclature/{id}/activate"
            },
            "warehouses_endpoints": {
                "list_warehouses": "/api/v1/warehouses/",
                "get_warehouse": "/api/v1/warehouses/{id}",
                "get_warehouse_by_code": "/api/v1/warehouses/code/{code}",
                "create_warehouse": "/api/v1/warehouses/",
                "update_warehouse": "/api/v1/warehouses/{id}",
                "delete_warehouse": "/api/v1/warehouses/{id}",
                "activate_warehouse": "/api/v1/warehouses/{id}/activate"
            },
            "stocks_endpoints": {
                "list_stocks": "/api/v1/stocks/",
                "get_stocks_summary": "/api/v1/stocks/summary",
                "get_stock": "/api/v1/stocks/{id}",
                "get_stock_by_nomenclature_and_warehouse": "/api/v1/stocks/nomenclature/{nomenclature_id}/warehouse/{warehouse_id}",
                "create_stock": "/api/v1/stocks/",
                "update_stock": "/api/v1/stocks/{id}",
                "delete_stock": "/api/v1/stocks/{id}"
            },
            "documents_endpoints": {
                "list_documents": "/api/v1/documents/",
                "get_document": "/api/v1/documents/{id}",
                "create_document": "/api/v1/documents/",
                "update_document": "/api/v1/documents/{id}",
                "delete_document": "/api/v1/documents/{id}",
                "post_document": "/api/v1/documents/{id}/post",
                "cancel_document": "/api/v1/documents/{id}/cancel",
                "create_document_item": "/api/v1/documents/{document_id}/items",
                "update_document_item": "/api/v1/documents/items/{item_id}",
                "delete_document_item": "/api/v1/documents/items/{item_id}"
            },
            "inventories_endpoints": {
                "list_inventories": "/api/v1/inventories/",
                "get_inventory": "/api/v1/inventories/{id}",
                "create_inventory": "/api/v1/inventories/",
                "update_inventory": "/api/v1/inventories/{id}",
                "delete_inventory": "/api/v1/inventories/{id}",
                "complete_inventory": "/api/v1/inventories/{id}/complete",
                "cancel_inventory": "/api/v1/inventories/{id}/cancel",
                "create_inventory_item": "/api/v1/inventories/{inventory_id}/items",
                "update_inventory_item": "/api/v1/inventories/items/{item_id}",
                "delete_inventory_item": "/api/v1/inventories/items/{item_id}"
            },
            "barcodes_endpoints": {
                "list_barcodes": "/api/v1/barcodes/",
                "get_barcode": "/api/v1/barcodes/{id}",
                "scan_barcode": "/api/v1/barcodes/scan/{barcode_value}",
                "create_barcode": "/api/v1/barcodes/",
                "update_barcode": "/api/v1/barcodes/{id}",
                "delete_barcode": "/api/v1/barcodes/{id}",
                "activate_barcode": "/api/v1/barcodes/{id}/activate",
                "deactivate_barcode": "/api/v1/barcodes/{id}/deactivate",
                "set_primary_barcode": "/api/v1/barcodes/{id}/set-primary"
            }
    }


@app.get("/health")
async def health_check():
    """Проверка здоровья сервера"""
    return {"status": "healthy"}

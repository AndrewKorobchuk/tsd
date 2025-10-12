from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.database import engine
from app.models import Base
from app.routers import oauth, units

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
        }
    }


@app.get("/health")
async def health_check():
    """Проверка здоровья сервера"""
    return {"status": "healthy"}

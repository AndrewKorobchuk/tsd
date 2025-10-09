# TSD API Server

Серверная часть приложения TSD на Python с FastAPI, PostgreSQL и OAuth 2.0 авторизацией.

## Структура проекта

```
python/tsd/
├── app/
│   ├── __init__.py
│   ├── config.py          # Конфигурация приложения
│   ├── database.py        # Настройка базы данных
│   ├── models.py          # SQLAlchemy модели (User, OAuthClient, OAuthToken)
│   ├── schemas.py         # Pydantic схемы
│   ├── oauth.py           # OAuth 2.0 авторизация
│   └── routers/
│       ├── __init__.py
│       └── oauth.py       # OAuth API эндпоинты
├── alembic/               # Миграции базы данных
├── main.py               # Основной файл приложения
├── requirements.txt      # Python зависимости
├── Dockerfile           # Docker образ
├── docker-compose.yml   # Docker Compose конфигурация
└── README.md           # Этот файл
```

## Запуск приложения

### С помощью Docker Compose (рекомендуется)

1. Убедитесь, что Docker и Docker Compose установлены
2. Перейдите в папку `python/tsd`
3. Запустите команду:

```bash
docker-compose up --build
```

Приложение будет доступно по адресу: http://localhost:8000

### Локальный запуск

1. Установите PostgreSQL
2. Создайте базу данных `tsd_db` с пользователем `tsd_user` и паролем `tsd_password`
3. Установите зависимости:

```bash
pip install -r requirements.txt
```

4. Запустите приложение:

```bash
uvicorn main:app --reload
```

## API Эндпоинты

### OAuth 2.0 Авторизация

- `POST /api/v1/oauth/register` - Регистрация нового OAuth клиента
- `POST /api/v1/oauth/token` - Получение OAuth токена
- `POST /api/v1/oauth/register-user` - Регистрация нового пользователя
- `GET /api/v1/oauth/me` - Получение информации о текущем пользователе
- `GET /api/v1/oauth/client-info` - Получение информации о текущем клиенте

### Другие эндпоинты

- `GET /` - Корневой эндпоинт с информацией об API
- `GET /health` - Проверка здоровья сервера
- `GET /docs` - Swagger документация API

## OAuth 2.0 Flow

### 1. Регистрация OAuth клиента

```bash
curl -X POST "http://localhost:8001/api/v1/oauth/register" \
     -H "Content-Type: application/json" \
     -d '{
       "client_name": "Mobile App",
       "redirect_uris": ["http://localhost:3000/callback"],
       "scope": "read write"
     }'
```

### 2. Регистрация пользователя

```bash
curl -X POST "http://localhost:8001/api/v1/oauth/register-user" \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser",
       "email": "test@example.com",
       "password": "testpassword"
     }'
```

### 3. Получение токена (Password Grant)

```bash
curl -X POST "http://localhost:8001/api/v1/oauth/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=password&client_id=YOUR_CLIENT_ID&username=testuser&password=testpassword&scope=read write"
```

### 4. Получение токена (Client Credentials Grant)

```bash
curl -X POST "http://localhost:8001/api/v1/oauth/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=client_credentials&client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&scope=read write"
```

### 5. Использование токена

```bash
curl -X GET "http://localhost:8001/api/v1/oauth/me" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Конфигурация

Настройки приложения находятся в файле `app/config.py`. Для изменения настроек в продакшене используйте переменные окружения:

- `DATABASE_URL` - URL базы данных
- `SECRET_KEY` - Секретный ключ для JWT токенов
- `ACCESS_TOKEN_EXPIRE_MINUTES` - Время жизни токена в минутах

## База данных

Приложение использует PostgreSQL. Таблицы создаются автоматически при первом запуске. Для управления миграциями используется Alembic.

### Создание миграции

```bash
alembic revision --autogenerate -m "Описание изменений"
```

### Применение миграций

```bash
alembic upgrade head
```

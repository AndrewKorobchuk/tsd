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

### Справочник единиц измерения

- `GET /api/v1/units/` - Получение списка единиц измерения
- `GET /api/v1/units/{id}` - Получение единицы измерения по ID
- `GET /api/v1/units/code/{code}` - Получение единицы измерения по коду
- `POST /api/v1/units/` - Создание новой единицы измерения
- `PUT /api/v1/units/{id}` - Обновление единицы измерения
- `DELETE /api/v1/units/{id}` - Удаление единицы измерения (деактивация)
- `PATCH /api/v1/units/{id}/activate` - Активация единицы измерения

### Справочник категорий номенклатуры

- `GET /api/v1/nomenclature-categories/` - Получение списка категорий номенклатуры
- `GET /api/v1/nomenclature-categories/{id}` - Получение категории по ID
- `GET /api/v1/nomenclature-categories/code/{code}` - Получение категории по коду
- `POST /api/v1/nomenclature-categories/` - Создание новой категории
- `PUT /api/v1/nomenclature-categories/{id}` - Обновление категории
- `DELETE /api/v1/nomenclature-categories/{id}` - Удаление категории (деактивация)
- `PATCH /api/v1/nomenclature-categories/{id}/activate` - Активация категории

### Справочник номенклатуры

- `GET /api/v1/nomenclature/` - Получение списка номенклатуры
- `GET /api/v1/nomenclature/{id}` - Получение номенклатуры по ID
- `GET /api/v1/nomenclature/code/{code}` - Получение номенклатуры по коду
- `POST /api/v1/nomenclature/` - Создание новой номенклатуры
- `PUT /api/v1/nomenclature/{id}` - Обновление номенклатуры
- `DELETE /api/v1/nomenclature/{id}` - Удаление номенклатуры (деактивация)
- `PATCH /api/v1/nomenclature/{id}/activate` - Активация номенклатуры

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

## Справочник единиц измерения

### 1. Получение списка единиц измерения

```bash
curl -X GET "http://localhost:8001/api/v1/units/" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 2. Поиск единиц измерения

```bash
curl -X GET "http://localhost:8001/api/v1/units/?search=кг" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 3. Получение единицы измерения по коду

```bash
curl -X GET "http://localhost:8001/api/v1/units/code/kg" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 4. Создание новой единицы измерения

```bash
curl -X POST "http://localhost:8001/api/v1/units/" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "code": "lb",
       "name": "Фунт",
       "short_name": "фунт",
       "description": "Единица измерения массы"
     }'
```

### 5. Обновление единицы измерения

```bash
curl -X PUT "http://localhost:8001/api/v1/units/1" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "Килограмм (обновлено)",
       "description": "Обновленное описание"
     }'
```

### 6. Деактивация единицы измерения

```bash
curl -X DELETE "http://localhost:8001/api/v1/units/1" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Справочник категорий номенклатуры

### 1. Получение списка категорий номенклатуры

```bash
curl -X GET "http://localhost:8001/api/v1/nomenclature-categories/" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 2. Создание новой категории

```bash
curl -X POST "http://localhost:8001/api/v1/nomenclature-categories/" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "code": "AUTO",
       "name": "Автомобили",
       "description": "Категория автомобилей и запчастей"
     }'
```

## Справочник номенклатуры

### 1. Получение списка номенклатуры

```bash
curl -X GET "http://localhost:8001/api/v1/nomenclature/" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 2. Фильтрация номенклатуры по категории

```bash
curl -X GET "http://localhost:8001/api/v1/nomenclature/?category_id=1" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 3. Поиск номенклатуры

```bash
curl -X GET "http://localhost:8001/api/v1/nomenclature/?search=хлеб" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 4. Создание новой номенклатуры

```bash
curl -X POST "http://localhost:8001/api/v1/nomenclature/" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "code": "BREAD_002",
       "category_id": 1,
       "name": "Хлеб черный",
       "base_unit_id": 11,
       "description_ru": "Черный хлеб из ржаной муки",
       "description_ua": "Чорний хліб з житнього борошна"
     }'
```

### Структура данных единицы измерения

```json
{
  "id": 1,
  "code": "kg",
  "name": "Килограмм",
  "short_name": "кг",
  "description": "Основная единица измерения массы в системе СИ",
  "is_active": true,
  "created_at": "2025-10-12T17:57:42",
  "updated_at": null
}
```

### Структура данных категории номенклатуры

```json
{
  "id": 1,
  "code": "FOOD",
  "name": "Продукты питания",
  "description": "Категория продуктов питания и напитков",
  "is_active": true,
  "created_at": "2025-10-12T18:59:20",
  "updated_at": null
}
```

### Структура данных номенклатуры

```json
{
  "id": 1,
  "code": "BREAD_001",
  "category_id": 1,
  "name": "Хлеб белый",
  "base_unit_id": 11,
  "description_ru": "Свежий белый хлеб",
  "description_ua": "Свіжий білий хліб",
  "is_active": true,
  "created_at": "2025-10-12T18:59:20",
  "updated_at": null,
  "category": {
    "id": 1,
    "code": "FOOD",
    "name": "Продукты питания",
    "description": "Категория продуктов питания и напитков",
    "is_active": true,
    "created_at": "2025-10-12T18:59:20",
    "updated_at": null
  },
  "base_unit": {
    "id": 11,
    "code": "pcs",
    "name": "Штука",
    "short_name": "шт",
    "description": "Единица измерения количества",
    "is_active": true,
    "created_at": "2025-10-12T17:57:42",
    "updated_at": null
  }
}
```

### Параметры запросов

- `skip` - количество записей для пропуска (по умолчанию: 0)
- `limit` - максимальное количество записей (по умолчанию: 100, максимум: 1000)
- `active_only` - показывать только активные единицы измерения (по умолчанию: true)
- `search` - поиск по названию, коду или краткому названию

### Тестовые данные

В системе уже добавлены:

**Единицы измерения (20 шт):**
- **Масса**: кг, г, т
- **Объем**: л, мл, м³
- **Длина**: м, см, мм
- **Площадь**: м²
- **Количество**: шт, кор, уп, компл, пар, дюж
- **Время**: ч, дн, мес, г

**Категории номенклатуры (5 шт):**
- FOOD - Продукты питания
- CLOTHES - Одежда
- ELECTRONICS - Электроника
- BOOKS - Книги
- HOUSEHOLD - Товары для дома

**Номенклатура (6 шт):**
- BREAD_001 - Хлеб белый (категория FOOD)
- MILK_001 - Молоко 3.2% (категория FOOD)
- SHIRT_001 - Рубашка мужская (категория CLOTHES)
- PHONE_001 - Смартфон (категория ELECTRONICS)
- BOOK_001 - Учебник математики (категория BOOKS)
- SOAP_001 - Мыло туалетное (категория HOUSEHOLD)

Для добавления тестовых данных выполните:

```bash
# Единицы измерения
docker-compose exec web python seed_units.py

# Категории номенклатуры и номенклатура
docker-compose exec web python seed_nomenclature.py
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

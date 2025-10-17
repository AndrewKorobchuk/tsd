-- Таблица для хранения ТСД устройств
CREATE TABLE tsd_devices (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(255) UNIQUE NOT NULL,  -- Уникальный ID устройства
    device_name VARCHAR(255),                -- Название устройства
    device_model VARCHAR(255),              -- Модель устройства
    android_version VARCHAR(50),            -- Версия Android
    app_version VARCHAR(50),                 -- Версия приложения
    prefix VARCHAR(10) UNIQUE NOT NULL,     -- Префикс для документов (например, "ТСД001")
    is_active BOOLEAN DEFAULT TRUE,          -- Активно ли устройство
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- Последний раз видели
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индекс для быстрого поиска по device_id
CREATE INDEX idx_tsd_devices_device_id ON tsd_devices(device_id);

-- Индекс для поиска по префиксу
CREATE INDEX idx_tsd_devices_prefix ON tsd_devices(prefix);


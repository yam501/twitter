-- =========================================================
-- Service users — БД users_db
-- Таблицы: roles, users, user_role (см. лабу 5, sql/users_service.sql)
-- Скрипт идемпотентный: выполняется при каждом старте приложения.
-- =========================================================

CREATE TABLE IF NOT EXISTS roles (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    firstname     VARCHAR(100) NOT NULL,
    lastname      VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    phone_number  VARCHAR(20)  UNIQUE,
    -- bcrypt-хеш (60 символов), пароль в открытом виде не хранится
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles (id) ON DELETE RESTRICT,

    PRIMARY KEY (user_id, role_id)
);

-- PK (user_id, role_id) покрывает выборку ролей пользователя.
CREATE INDEX IF NOT EXISTS idx_user_role_role ON user_role (role_id);

-- Справочные данные: без них регистрация не сможет выдать роль subscriber
INSERT INTO roles (name) VALUES
    ('administrator'),
    ('producer'),
    ('subscriber'),
    ('user')
ON CONFLICT (name) DO NOTHING;

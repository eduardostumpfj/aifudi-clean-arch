BEGIN;

-- ROLES --
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    value VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(50) NOT NULL
);

ALTER TABLE roles ALTER COLUMN id SET DEFAULT gen_random_uuid();

INSERT INTO roles (id, value, label)
VALUES
    ('00000000-0000-4000-8000-000000000001', 'ADMIN', 'Administrador'),
    ('00000000-0000-4000-8000-000000000002', 'OWNER', 'Proprietário'),
    ('00000000-0000-4000-8000-000000000003', 'USER', 'Usuário')
ON CONFLICT (value) DO NOTHING;

-- SUBROLES --
CREATE TABLE IF NOT EXISTS subroles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES roles(id),
    value VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(50) NOT NULL
);

ALTER TABLE subroles ALTER COLUMN id SET DEFAULT gen_random_uuid();

INSERT INTO subroles (id, role_id, value, label)
VALUES
    ('00000000-0000-4000-8000-000000000011',
     (SELECT id FROM roles WHERE value = 'ADMIN'), 'admin', 'Administrador'),
    ('00000000-0000-4000-8000-000000000012',
     (SELECT id FROM roles WHERE value = 'OWNER'), 'mainOwner', 'Proprietário principal'),
    ('00000000-0000-4000-8000-000000000013',
     (SELECT id FROM roles WHERE value = 'USER'), 'mainUser', 'Usuário')
ON CONFLICT (value) DO NOTHING;

-- USERS --
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_subrole_id UUID NOT NULL REFERENCES subroles(id),
    name VARCHAR(100) NOT NULL,
    login VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    hashed_password VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL
);

ALTER TABLE users ALTER COLUMN id SET DEFAULT gen_random_uuid();

INSERT INTO users (id, client_subrole_id, name, login, email, hashed_password, address)
SELECT seed.id::UUID, subroles.id, seed.name, seed.login, seed.email,
       crypt(seed.password, gen_salt('bf', 10)), seed.address
FROM (VALUES
    ('00000000-0000-4000-8000-000000000100', 'admin', 'admin', 'admin', 'admin@example.com', 'admin123', 'Rua das Flores, 10, Salvador - BA'),
    ('00000000-0000-4000-8000-000000000101', 'mainUser', 'Ana Silva', 'anasilva', 'ana.user@example.com', 'ana123', 'Rua das Flores, 10, Salvador - BA'),
    ('00000000-0000-4000-8000-000000000102', 'mainOwner', 'Bruno Santos', 'brunosantos', 'bruno.owner@example.com', 'bruno123', 'Rua das Palmeiras, 20, Salvador - BA'),
    ('00000000-0000-4000-8000-000000000103', 'mainOwner', 'Carla Oliveira', 'carlaoliveira', 'carla.owner@example.com', 'carla123', 'Avenida Oceânica, 30, Salvador - BA'),
    ('00000000-0000-4000-8000-000000000104', 'mainUser', 'Diego Souza', 'diegosouza', 'diego.user@example.com', 'diego123', 'Rua da Paz, 40, Salvador - BA'),
    ('00000000-0000-4000-8000-000000000105', 'mainUser', 'Elisa Costa', 'elisacosta', 'elisa.user@example.com', 'elisa123', 'Rua do Sol, 50, Salvador - BA')
) AS seed(id, subrole_value, name, login, email, password, address)
JOIN subroles ON subroles.value = seed.subrole_value
ON CONFLICT DO NOTHING;

-- KITCHENS --
CREATE TABLE IF NOT EXISTS kitchens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    value VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(50) NOT NULL
);

ALTER TABLE kitchens ALTER COLUMN id SET DEFAULT gen_random_uuid();

INSERT INTO kitchens (id, value, label)
VALUES
    ('00000000-0000-4000-8000-000000000201', 'ITALIAN', 'Italiana'),
    ('00000000-0000-4000-8000-000000000202', 'ARABIC', 'Árabe'),
    ('00000000-0000-4000-8000-000000000203', 'BRAZILIAN', 'Brasileira'),
    ('00000000-0000-4000-8000-000000000204', 'JAPANESE', 'Japonesa'),
    ('00000000-0000-4000-8000-000000000205', 'MEXICAN', 'Mexicana'),
    ('00000000-0000-4000-8000-000000000206', 'CHINESE', 'Chinesa'),
    ('00000000-0000-4000-8000-000000000207', 'INDIAN', 'Indiana'),
    ('00000000-0000-4000-8000-000000000208', 'FRENCH', 'Francesa')
ON CONFLICT (value) DO NOTHING;

-- RESTAURANTS
CREATE TABLE IF NOT EXISTS restaurants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    kitchen_id UUID NOT NULL REFERENCES kitchens(id),
    name VARCHAR(50) NOT NULL,
    address VARCHAR(255) NOT NULL,
    opening_time VARCHAR(255) NOT NULL
);

ALTER TABLE restaurants ALTER COLUMN id SET DEFAULT gen_random_uuid();

CREATE INDEX IF NOT EXISTS idx_restaurants_kitchen_id ON restaurants(kitchen_id);

-- Restaurantes de exemplo para os vínculos iniciais.
INSERT INTO restaurants (id, kitchen_id, name, address, opening_time)
SELECT seed.id::UUID, kitchens.id, seed.name, seed.address, seed.opening_time
FROM (VALUES
    ('00000000-0000-4000-8000-000000000301', 'BRAZILIAN', 'Restaurante do Bruno', 'Rua das Palmeiras, 20, Salvador - BA', '11:00 - 22:00'),
    ('00000000-0000-4000-8000-000000000302', 'ITALIAN', 'Restaurante da Carla', 'Avenida Oceânica, 30, Salvador - BA', '18:00 - 23:00')
) AS seed(id, kitchen_value, name, address, opening_time)
JOIN kitchens ON kitchens.value = seed.kitchen_value
ON CONFLICT (id) DO NOTHING;

-- MENU ITEMS --
CREATE TABLE IF NOT EXISTS menu_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID NOT NULL REFERENCES restaurants(id),
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    picture VARCHAR(255) NOT NULL,
    has_delivery BOOLEAN NOT NULL
);

ALTER TABLE menu_items ALTER COLUMN id SET DEFAULT gen_random_uuid();

CREATE INDEX IF NOT EXISTS idx_menu_items_restaurant_id ON menu_items(restaurant_id);

-- MEMBERSHIP --
CREATE TABLE IF NOT EXISTS memberships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    subrole_id UUID NOT NULL REFERENCES subroles(id),
    restaurant_id UUID NOT NULL REFERENCES restaurants(id),
    CONSTRAINT uk_membership_user_restaurant UNIQUE (user_id, restaurant_id)
);

ALTER TABLE memberships ALTER COLUMN id SET DEFAULT gen_random_uuid();

CREATE INDEX IF NOT EXISTS idx_memberships_subrole_id ON memberships(subrole_id);
CREATE INDEX IF NOT EXISTS idx_memberships_restaurant_id ON memberships(restaurant_id);

-- IDs dos vínculos são gerados pelo banco. Reexecutar preserva vínculos existentes.
INSERT INTO memberships (user_id, subrole_id, restaurant_id)
SELECT users.id, subroles.id, restaurants.id
FROM (VALUES
    ('admin', 'admin', '00000000-0000-4000-8000-000000000301'),
    ('admin', 'admin', '00000000-0000-4000-8000-000000000302'),
    ('brunosantos', 'mainOwner', '00000000-0000-4000-8000-000000000301'),
    ('carlaoliveira', 'mainOwner', '00000000-0000-4000-8000-000000000302'),
    ('anasilva', 'mainUser', '00000000-0000-4000-8000-000000000301'),
    ('diegosouza', 'mainUser', '00000000-0000-4000-8000-000000000301'),
    ('elisacosta', 'mainUser', '00000000-0000-4000-8000-000000000302')
) AS seed(login, subrole_value, restaurant_id)
JOIN users ON users.login = seed.login
JOIN subroles ON subroles.value = seed.subrole_value
JOIN restaurants ON restaurants.id = seed.restaurant_id::UUID
ON CONFLICT (user_id, restaurant_id) DO NOTHING;

COMMIT;

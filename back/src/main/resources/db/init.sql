-- Le Bar'app - Script d'initialisation du schema (PostgreSQL)
-- Au runtime, le schema est cree par Hibernate (ddl-auto) et les donnees de demo
-- par le seeder applicatif. Ce script reproduit le schema pour une base vide.

-- Staff (barmakers). Le client est anonyme (modele QR a table), pas de compte client.
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- Tables du bar, identifiees par le QR code.
CREATE TABLE IF NOT EXISTS bar_table (
    id      BIGSERIAL PRIMARY KEY,
    label   VARCHAR(40) NOT NULL,
    qr_slug VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS categories (
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(80) NOT NULL UNIQUE,
    position INTEGER     NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS ingredients (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS cocktails (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(120) NOT NULL,
    description  TEXT,
    is_available BOOLEAN      NOT NULL DEFAULT TRUE,
    category_id  BIGINT       NOT NULL REFERENCES categories (id)
);

-- Liaison cocktail/ingredient portant la quantite (recette propre du bar).
CREATE TABLE IF NOT EXISTS cocktail_ingredients (
    id            BIGSERIAL PRIMARY KEY,
    cocktail_id   BIGINT NOT NULL REFERENCES cocktails (id),
    ingredient_id BIGINT NOT NULL REFERENCES ingredients (id),
    measure       VARCHAR(60),
    UNIQUE (cocktail_id, ingredient_id)
);

-- Image du cocktail stockee en base (telechargee et possedee par l'application).
CREATE TABLE IF NOT EXISTS cocktail_image (
    id           BIGSERIAL PRIMARY KEY,
    cocktail_id  BIGINT      NOT NULL UNIQUE REFERENCES cocktails (id),
    content_type VARCHAR(50) NOT NULL,
    data         BYTEA       NOT NULL
);

-- Prix par taille (S, M, L).
CREATE TABLE IF NOT EXISTS cocktail_sizes (
    id          BIGSERIAL PRIMARY KEY,
    cocktail_id BIGINT       NOT NULL REFERENCES cocktails (id),
    size        VARCHAR(5)   NOT NULL,
    price       NUMERIC(6,2) NOT NULL,
    UNIQUE (cocktail_id, size)
);

CREATE TABLE IF NOT EXISTS orders (
    id            BIGSERIAL PRIMARY KEY,
    table_id      BIGINT      NOT NULL REFERENCES bar_table (id),
    customer_name VARCHAR(80),
    status        VARCHAR(20) NOT NULL DEFAULT 'ORDERED',
    created_at    TIMESTAMP   NOT NULL DEFAULT now()
);

-- Une ligne = un cocktail commande, avec son prix fige et sa progression de preparation.
CREATE TABLE IF NOT EXISTS order_items (
    id                 BIGSERIAL PRIMARY KEY,
    order_id           BIGINT       NOT NULL REFERENCES orders (id),
    cocktail_id        BIGINT       NOT NULL REFERENCES cocktails (id),
    size               VARCHAR(5)   NOT NULL,
    unit_price         NUMERIC(6,2) NOT NULL,
    preparation_status VARCHAR(30)  NOT NULL DEFAULT 'INGREDIENTS'
);

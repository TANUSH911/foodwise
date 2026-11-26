-- =====================================================================
-- FoodWise schema (portable SQL: runs on MySQL 8+ and H2 in MySQL mode)
-- Never edit a migration that has already run. To change the schema,
-- add a new file: V3__describe_change.sql (see docs/DATABASE.md).
-- =====================================================================

-- One row per food item in the pantry. Items are never deleted when
-- eaten or thrown away: status changes so waste statistics keep history.
CREATE TABLE food_item (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100)  NOT NULL,
    category      VARCHAR(30)   NOT NULL,              -- FoodCategory enum name
    quantity      DECIMAL(10,2) NOT NULL,
    unit          VARCHAR(20)   NOT NULL,
    price         DECIMAL(10,2) NULL,                  -- optional, in rupees
    purchase_date DATE          NOT NULL,
    expiry_date   DATE          NOT NULL,
    status        VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    status_date   DATE          NULL,                  -- day it was consumed / wasted
    CONSTRAINT pk_food_item PRIMARY KEY (id),
    CONSTRAINT chk_food_item_status   CHECK (status IN ('ACTIVE', 'CONSUMED', 'WASTED')),
    CONSTRAINT chk_food_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_food_item_price    CHECK (price IS NULL OR price >= 0),
    CONSTRAINT chk_food_item_dates    CHECK (expiry_date >= purchase_date)
);
CREATE INDEX idx_food_item_status_expiry ON food_item (status, expiry_date);
CREATE INDEX idx_food_item_status_date   ON food_item (status, status_date);

-- Local recipe catalogue (seeded in V2). Images are hot-linked from TheMealDB.
CREATE TABLE recipe (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    name         VARCHAR(120)  NOT NULL,
    cuisine      VARCHAR(40)   NULL,
    prep_minutes INT           NULL,
    servings     INT           NULL,
    difficulty   VARCHAR(10)   NULL,                   -- Easy / Medium / Hard
    image_url    VARCHAR(255)  NULL,
    instructions VARCHAR(2000) NOT NULL,
    CONSTRAINT pk_recipe PRIMARY KEY (id),
    CONSTRAINT uq_recipe_name UNIQUE (name)
);

-- Ingredients of a recipe. ingredient_name is matched against pantry item
-- names (case-insensitive, singular/plural tolerant) by IngredientMatcher.
CREATE TABLE recipe_ingredient (
    recipe_id       BIGINT       NOT NULL,
    ingredient_name VARCHAR(60)  NOT NULL,
    amount          VARCHAR(40)  NULL,                 -- display only, e.g. "2 cups"
    CONSTRAINT pk_recipe_ingredient PRIMARY KEY (recipe_id, ingredient_name),
    CONSTRAINT fk_recipe_ingredient_recipe FOREIGN KEY (recipe_id)
        REFERENCES recipe (id) ON DELETE CASCADE
);

-- In-app expiry alerts. At most one alert per food item.
CREATE TABLE notification (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    food_item_id BIGINT       NOT NULL,
    message      VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read      BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_notification PRIMARY KEY (id),
    CONSTRAINT uq_notification_food_item UNIQUE (food_item_id),
    CONSTRAINT fk_notification_food_item FOREIGN KEY (food_item_id)
        REFERENCES food_item (id) ON DELETE CASCADE
);
CREATE INDEX idx_notification_read ON notification (is_read, created_at);

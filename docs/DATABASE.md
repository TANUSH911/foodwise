# Database guide

FoodWise uses a **SQL** database. The same SQL runs on two engines:

| Engine | When | How to use |
|---|---|---|
| **H2** (file `./data/foodwise.mv.db`, MySQL mode) | Development, zero setup | Default. Just run `./mvnw` |
| **MySQL 8** | Final demo / production | `./mvnw -Dspring-boot.run.profiles=mysql` |

## Who owns the schema

**Flyway** creates and changes tables from the SQL files in
`src/main/resources/db/migration`. Hibernate is set to `ddl-auto=none`, so it
never touches tables on its own. That keeps the schema readable and identical
on every laptop.

| File | What it does |
|---|---|
| `V1__create_schema.sql` | Creates all tables, keys, checks and indexes |
| `V2__seed_recipes.sql` | Inserts the 12 recipes and their ingredients |

Demo pantry items are **not** in SQL: `DemoDataSeeder.java` inserts them when
the `food_item` table is empty, with dates relative to today so the demo never
goes stale. Switch it off with `foodwise.demo-data=false`.

## Tables

```
food_item ─────────────< notification          recipe ─────< recipe_ingredient
  id (PK)                  id (PK)                 id (PK)       recipe_id (PK, FK)
  name                     food_item_id (FK, UQ)   name (UQ)     ingredient_name (PK)
  category                 message                 cuisine       amount
  quantity, unit           created_at              prep_minutes
  price (nullable)         is_read                 servings
  purchase_date                                    difficulty
  expiry_date                                      image_url
  status  ACTIVE|CONSUMED|WASTED                   instructions
  status_date
```

Rules worth knowing:

- Items are **never deleted** when eaten or thrown away. `status` changes and
  `status_date` records the day. Waste statistics depend on this history.
- `category` stores the Java enum name (`DAIRY`, `VEGETABLES`, ...). See `FoodCategory.java`.
- Urgency (Expired / Use today / Use soon / Fresh) is **computed** from
  `expiry_date`, not stored. See `Urgency.java`.
- Deleting a food item deletes its notification (`ON DELETE CASCADE`).
- CHECK constraints stop bad data: quantity > 0, price >= 0, expiry >= purchase.

## Changing the schema (important)

1. **Never edit a migration that already ran.** Flyway checksums them and the app will refuse to start.
2. Add a new file with the next number, e.g. `V3__add_shopping_list.sql`.
3. Write SQL that works on both MySQL and H2 (stick to `VARCHAR`, `INT`,
   `BIGINT`, `DECIMAL`, `DATE`, `TIMESTAMP`, `BOOLEAN`, `AUTO_INCREMENT`).
4. Update the matching `@Entity` class so its fields match the new columns.
5. Run `./mvnw test`. `FoodWiseApplicationTest` boots the app on a fresh database.

If your local H2 database gets into a bad state, stop the app and delete the `data/` folder.

## Looking at the data

- **H2:** open http://localhost:8080/h2-console, JDBC URL `jdbc:h2:file:./data/foodwise;MODE=MySQL`, user `sa`, empty password.
- **MySQL:** any client (MySQL Workbench, DBeaver, `mysql` CLI).

Example: monthly waste in plain SQL (MySQL syntax):

```sql
SELECT DATE_FORMAT(status_date, '%Y-%m') AS month,
       COUNT(*)   AS items_wasted,
       SUM(price) AS money_wasted
FROM food_item
WHERE status = 'WASTED'
GROUP BY month
ORDER BY month;
```

The app runs the same query in `FoodItemRepository.monthlyTotals` (written in JPQL so it works on both engines).

## MySQL setup (once)

```sql
CREATE DATABASE foodwise CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Credentials are read from environment variables, so they are never committed:

```bash
export DB_USER=root DB_PASSWORD=yourpassword   # optional: DB_HOST, DB_PORT
./mvnw -Dspring-boot.run.profiles=mysql
```

Flyway creates the tables on first start.

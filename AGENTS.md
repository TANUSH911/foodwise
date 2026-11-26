# Notes for contributors and AI coding assistants

Read this before changing code. It keeps the project consistent and the database clean.

## Stack
Java 21 · Spring Boot 3.5 · Vaadin Flow 24 (UI in Java) · Spring Data JPA / Hibernate · Flyway · H2 (dev) / MySQL (demo).
Build with `./mvnw`. Test with `./mvnw test`.

## Architecture rules
- Layers: `view` → `service` → `repository` → `entity`. Views never call repositories directly.
- Business rules go in `service/` as plain Java, and are unit-testable without Spring where possible
  (see `RecipeService.rank`, `PriorityService.eatFirst`, `IngredientMatcher`).
- Pass `LocalDate today` into logic instead of calling `LocalDate.now()` deep inside, so tests can pin the date.
- Records are used for read-only results (`PriorityItem`, `RecipeMatch`, `WasteStats`, `MonthlyWaste`).
- No Lombok; plain getters and setters.

## Database rules
- The schema is owned by Flyway SQL files in `src/main/resources/db/migration`. `ddl-auto=none`.
- **Never edit an existing `V*__*.sql` file.** Add a new `V<next>__<what>.sql`.
- SQL must run on both MySQL 8 and H2 in MySQL mode. Avoid engine-specific functions in migrations.
- Keep entities in sync with tables: snake_case columns ↔ camelCase fields (Spring's default naming).
- Enums are stored as strings (`@Enumerated(EnumType.STRING)`).
- Food items are not deleted when used: set `status` to CONSUMED/WASTED and `statusDate` (stats rely on it).
- More detail: `docs/DATABASE.md`.

## UI rules
- Every screen is a `@Route` class in `view/` using `layout = MainLayout.class` and implementing `Refreshable`.
- Styling lives only in `src/main/frontend/themes/foodwise/styles.css`. Use the `--fw-*` tokens, not raw colours.
  Class names start with `fw-`.
- Icons: `Ui.icon("name")` loads `META-INF/resources/icons/<name>.svg` (Lucide). To add one, download it from
  https://lucide.dev and drop the SVG in that folder. Do not use emoji as icons.
- Accessibility: icon-only buttons need `setAriaLabel`; never show urgency by colour alone (badges include text);
  keep touch targets ≥ 44px.
- Toasts: `Ui.toast(...)` / `Ui.warningToast(...)`.

## Where to add common features
| Feature | Start here |
|---|---|
| Shopping list | new migration + entity `ShoppingItem`, `ShoppingService`, `ShoppingView`, nav link in `MainLayout` |
| Partial use (reduce quantity) | `FoodItemService` new method, button in `FoodItemCard` |
| Email alerts | add `spring-boot-starter-mail`, send from `NotificationService.createExpiryAlerts` |
| More recipes | new migration `V3__more_recipes.sql`, same pattern as `V2__seed_recipes.sql` |
| Login / multi-user | Spring Security + `user_id` column on `food_item` (new migration) |

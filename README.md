# FoodWise: Smart Food Expiry & Waste Predictor

FoodWise tracks the food you buy, warns you before it expires, suggests recipes
that use the ingredients about to go bad, and shows how much food (and money)
you waste each month.

**100% Java:** Spring Boot 3 backend, Vaadin Flow UI (no HTML/JS written by hand), SQL database (MySQL or H2) via JPA/Hibernate and Flyway.

## Features

| Feature | Where in the app | Code |
|---|---|---|
| Add / edit / delete food items, mark **Eaten** or **Wasted** | Pantry, `+` button | `FoodItemService`, `PantryView`, `ItemFormDialog` |
| Automatic urgency: Expired · Use today · Use soon (≤3 days) · Fresh | Colour-coded cards, filters | `Urgency` |
| Shelf-life defaults (category pre-fills expiry date) | Add-item form | `FoodCategory.defaultShelfDays` |
| Dashboard with freshness gauge and summary cards | Home | `DashboardView` |
| Daily expiry alerts + bell with unread count | Bell icon | `ExpiryCheckScheduler`, `NotificationService` |
| ⭐ **"Use these first"** ranking with a reason per item | Home | `PriorityService` |
| ⭐ **Smart recipes** ranked by soon-to-expire ingredients, "Missing: …", **Cook this** | Recipes | `RecipeService`, `IngredientMatcher` |
| Waste stats: per month, by category, eaten vs wasted, insight | Stats | `StatsService`, `StatsView` |

## Run it

**Needs:** JDK 21. Nothing else: Maven comes with the wrapper (`mvnw`), the database is H2 by default, and Vaadin downloads Node.js itself if needed.

```bash
git clone https://github.com/TANUSH911/foodwise.git
cd foodwise
./mvnw
```

Open http://localhost:8080. The first start takes a few minutes while dependencies download.
Sample data (20 pantry items + 5 months of history) is added automatically.

On Windows use `mvnw.cmd`. In **IntelliJ IDEA**: *Open* the folder, let it import Maven,
then run `FoodWiseApplication` (IntelliJ can download JDK 21 for you under *File → Project Structure → SDKs*).

| Task | Command |
|---|---|
| Run tests | `./mvnw test` |
| Run against MySQL | `./mvnw -Dspring-boot.run.profiles=mysql` (see [docs/DATABASE.md](docs/DATABASE.md)) |
| Production jar | `./mvnw -Pproduction package` then `java -jar target/foodwise-1.0.0.jar` |
| Reset local data | stop the app, delete the `data/` folder |

## Project structure

```
src/main/java/com/foodwise
├── FoodWiseApplication.java   entry point, theme, fonts
├── entity/       JPA entities mapped to SQL tables + enums (FoodCategory, ItemStatus, Urgency)
├── repository/   Spring Data repositories (SQL access) + query result records
├── service/      business logic: pantry, priority ranking, recipes, alerts, stats
├── scheduler/    daily expiry check
├── config/       demo data seeder
└── view/         Vaadin screens (MainLayout, Dashboard, Pantry, Recipes, Stats)
    └── components/  reusable UI pieces (cards, dialogs, Ui helpers)

src/main/resources
├── application.properties         H2 (default) settings
├── application-mysql.properties   MySQL settings
├── db/migration/                  Flyway SQL: schema + recipe seed
└── META-INF/resources/icons/      Lucide SVG icons (ISC licence)

src/main/frontend/themes/foodwise/styles.css   all styling (design tokens at the top)
docs/DATABASE.md                               schema, migrations, MySQL setup
AGENTS.md                                      conventions for contributors and AI coding tools
```

## How the unique feature works

1. Every active item gets an **urgency weight** from its days left:
   today = 10, tomorrow = 8, 2 days = 6, 3 days = 4, ≤7 days = 2, later = 1, expired = 0.
2. **Use these first** lists items by weight, with a reason such as *"Expires tomorrow · 1 L left"*.
3. Each recipe's score is the **sum of the weights of the pantry items it uses**
   (`IngredientMatcher` handles case and plurals: "Tomatoes" matches "tomato").
   Recipes that rescue the most soon-to-expire food come first.
4. **Cook this** marks the used items as eaten, which feeds straight into the waste stats.

## Team split

| Member | Owns |
|---|---|
| 1: Core / Data | `entity/`, `repository/`, `FoodItemService`, `db/migration/`, `Urgency` |
| 2: UI | `MainLayout`, `DashboardView`, `PantryView`, `components/`, `styles.css` |
| 3: Alerts + Stats | `NotificationService`, `ExpiryCheckScheduler`, `StatsService`, `StatsView` |
| 4: Recipes | `PriorityService`, `RecipeService`, `IngredientMatcher`, `RecipesView`, recipe seed SQL |

Workflow: one branch per person, small PRs, merge to `main` at least daily, and run `./mvnw test` before pushing.

## Demo script (~3 min)

1. **Pantry → +**: add "Milk", Dairy (expiry fills in automatically), set expiry to tomorrow. A heads-up toast appears.
2. **Home**: the gauge, the summary cards and **Use these first** show the urgent items.
3. **Bell**: open the expiry alerts.
4. **Recipes**: the top picks use the expiring milk and tomatoes. Open one and check the *Missing* list.
5. **Cook this**: the ingredients are marked eaten.
6. **Pantry**: mark an item **Wasted**.
7. **Stats**: the charts and the insight update.

## Not included (by design)

Login / multiple users, barcode scanning, email alerts and an ML predictor are out of scope for the
4-day build (see the project plan). The rule-based insight on the Stats page is the stand-in for the ML predictor.

Recipe photos come from [TheMealDB](https://www.themealdb.com) and need an internet connection; offline, a placeholder is shown.

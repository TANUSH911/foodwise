package com.foodwise;

import com.foodwise.entity.ItemStatus;
import com.foodwise.repository.FoodItemRepository;
import com.foodwise.repository.RecipeRepository;
import com.foodwise.service.NotificationService;
import com.foodwise.service.RecipeService;
import com.foodwise.service.StatsService;
import com.foodwise.service.WasteStats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Boots the app on an in-memory H2 database: runs the Flyway migrations, demo seeder and SQL queries. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:foodwise-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "vaadin.launch-browser=false"
})
class FoodWiseApplicationTest {

    @Autowired FoodItemRepository foodItems;
    @Autowired RecipeRepository recipes;
    @Autowired NotificationService notifications;
    @Autowired RecipeService recipeService;
    @Autowired StatsService stats;

    @Test
    void schemaSeedAndQueriesWork() {
        assertEquals(12, recipes.count());
        assertEquals(20, foodItems.countByStatus(ItemStatus.ACTIVE));
        assertTrue(notifications.unreadCount() > 0, "startup expiry check should create alerts");

        WasteStats waste = stats.wasteStats(LocalDate.now());
        assertEquals(StatsService.MONTHS, waste.monthly().size());
        assertFalse(waste.byCategory().isEmpty());
        assertEquals("DAIRY", waste.byCategory().get(0).category().name());

        assertTrue(recipeService.suggest(LocalDate.now()).get(0).score() > 0);
    }
}

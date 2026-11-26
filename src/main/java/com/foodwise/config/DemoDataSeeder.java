package com.foodwise.config;

import com.foodwise.entity.FoodCategory;
import com.foodwise.entity.FoodItem;
import com.foodwise.entity.ItemStatus;
import com.foodwise.repository.FoodItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.foodwise.entity.FoodCategory.*;

/**
 * Fills an empty pantry with sample data so every screen has something to show.
 * Dates are relative to today, so the demo never goes stale.
 * Turn off with {@code foodwise.demo-data=false}.
 */
@Component
@ConditionalOnProperty(name = "foodwise.demo-data", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final FoodItemRepository repository;

    public DemoDataSeeder(FoodItemRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now();
        List<FoodItem> items = new ArrayList<>();

        // Active pantry: name, category, qty, unit, price, bought (days ago), expires (days from today)
        items.add(active("Milk", DAIRY, 1, "L", 68, 3, 1, today));
        items.add(active("Tomatoes", VEGETABLES, 6, "pcs", 60, 4, 0, today));
        items.add(active("Spinach", VEGETABLES, 250, "g", 40, 3, 1, today));
        items.add(active("Paneer", DAIRY, 200, "g", 95, 2, 2, today));
        items.add(active("Bananas", FRUITS, 4, "pcs", 50, 5, 2, today));
        items.add(active("Chicken breast", MEAT, 500, "g", 280, 1, 1, today));
        items.add(active("Bread", BAKERY, 1, "pack", 45, 3, -1, today));
        items.add(active("Yogurt", DAIRY, 400, "g", 70, 4, 3, today));
        items.add(active("Eggs", DAIRY, 12, "pcs", 84, 5, 9, today));
        items.add(active("Onions", VEGETABLES, 1, "kg", 40, 6, 14, today));
        items.add(active("Potatoes", VEGETABLES, 2, "kg", 70, 6, 20, today));
        items.add(active("Carrots", VEGETABLES, 500, "g", 35, 2, 5, today));
        items.add(active("Capsicum", VEGETABLES, 2, "pcs", 30, 2, 4, today));
        items.add(active("Cream", DAIRY, 200, "ml", 65, 2, 6, today));
        items.add(active("Butter", DAIRY, 100, "g", 56, 10, 30, today));
        items.add(active("Cheese slices", DAIRY, 10, "pcs", 120, 7, 12, today));
        items.add(active("Green peas", FROZEN, 500, "g", 90, 12, 120, today));
        items.add(active("Basmati rice", GRAINS, 5, "kg", 650, 30, 300, today));
        items.add(active("Lentils (toor dal)", GRAINS, 1, "kg", 160, 20, 200, today));
        items.add(active("Garlic", VEGETABLES, 100, "g", 30, 8, 10, today));

        // History for the stats charts: consumed / wasted over the last 5 months.
        String[][] history = {
                // name, category, price, months ago, wasted?
                {"Milk", "DAIRY", "68", "5", "W"}, {"Bread", "BAKERY", "45", "5", "W"}, {"Rice", "GRAINS", "120", "5", "C"},
                {"Yogurt", "DAIRY", "70", "4", "W"}, {"Spinach", "VEGETABLES", "40", "4", "W"}, {"Paneer", "DAIRY", "95", "4", "W"},
                {"Apples", "FRUITS", "120", "4", "C"}, {"Eggs", "DAIRY", "84", "4", "C"},
                {"Milk", "DAIRY", "68", "3", "W"}, {"Bananas", "FRUITS", "50", "3", "W"}, {"Tomatoes", "VEGETABLES", "60", "3", "C"},
                {"Chicken", "MEAT", "280", "3", "C"}, {"Onions", "VEGETABLES", "40", "3", "C"},
                {"Cream", "DAIRY", "65", "2", "W"}, {"Coriander", "VEGETABLES", "15", "2", "W"}, {"Bread", "BAKERY", "45", "2", "C"},
                {"Milk", "DAIRY", "68", "2", "C"}, {"Carrots", "VEGETABLES", "35", "2", "C"},
                {"Yogurt", "DAIRY", "70", "1", "W"}, {"Mangoes", "FRUITS", "200", "1", "C"}, {"Eggs", "DAIRY", "84", "1", "C"},
                {"Potatoes", "VEGETABLES", "70", "1", "C"}, {"Fish", "MEAT", "350", "1", "C"},
                {"Lettuce", "VEGETABLES", "60", "0", "W"}, {"Milk", "DAIRY", "68", "0", "C"}, {"Cheese", "DAIRY", "120", "0", "C"},
        };
        for (String[] h : history) {
            LocalDate when = today.minusMonths(Long.parseLong(h[3])).withDayOfMonth(Math.min(today.getDayOfMonth(), 20));
            if (when.isAfter(today)) when = today;
            FoodItem item = new FoodItem(h[0], FoodCategory.valueOf(h[1]), BigDecimal.ONE, "pcs",
                    new BigDecimal(h[2]), when.minusDays(6), when);
            item.setStatus("W".equals(h[4]) ? ItemStatus.WASTED : ItemStatus.CONSUMED);
            item.setStatusDate(when);
            items.add(item);
        }

        repository.saveAll(items);
        log.info("Seeded {} demo food items", items.size());
    }

    private static FoodItem active(String name, FoodCategory category, double qty, String unit,
                                   int price, int boughtDaysAgo, int expiresInDays, LocalDate today) {
        return new FoodItem(name, category, BigDecimal.valueOf(qty), unit, BigDecimal.valueOf(price),
                today.minusDays(boughtDaysAgo), today.plusDays(expiresInDays));
    }
}

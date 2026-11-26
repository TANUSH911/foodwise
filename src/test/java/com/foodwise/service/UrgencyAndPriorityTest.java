package com.foodwise.service;

import com.foodwise.entity.FoodCategory;
import com.foodwise.entity.FoodItem;
import com.foodwise.entity.Urgency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrgencyAndPriorityTest {

    static final LocalDate TODAY = LocalDate.of(2026, 11, 26);

    static FoodItem item(String name, int expiresInDays) {
        return new FoodItem(name, FoodCategory.OTHER, BigDecimal.ONE, "pcs", null,
                TODAY.minusDays(5), TODAY.plusDays(expiresInDays));
    }

    @Test
    void urgencyCategories() {
        assertEquals(Urgency.EXPIRED, Urgency.of(TODAY.minusDays(1), TODAY));
        assertEquals(Urgency.USE_TODAY, Urgency.of(TODAY, TODAY));
        assertEquals(Urgency.USE_SOON, Urgency.of(TODAY.plusDays(Urgency.SOON_DAYS), TODAY));
        assertEquals(Urgency.FRESH, Urgency.of(TODAY.plusDays(Urgency.SOON_DAYS + 1), TODAY));
        assertEquals("Expires tomorrow", Urgency.describe(1));
        assertEquals("Expired 3 days ago", Urgency.describe(-3));
    }

    @Test
    void eatFirstPutsSoonestFirstAndSkipsExpired() {
        List<PriorityItem> list = new PriorityService().eatFirst(
                List.of(item("Rice", 100), item("Milk", 1), item("Old bread", -2), item("Spinach", 0)), TODAY, 10);

        assertEquals(List.of("Spinach", "Milk", "Rice"), list.stream().map(p -> p.item().getName()).toList());
        assertEquals("Expires today · 1 pcs left", list.get(0).reason());
    }
}

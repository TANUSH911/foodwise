package com.foodwise.service;

import com.foodwise.entity.FoodItem;
import com.foodwise.entity.Recipe;
import com.foodwise.entity.RecipeIngredient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.foodwise.service.UrgencyAndPriorityTest.TODAY;
import static com.foodwise.service.UrgencyAndPriorityTest.item;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeServiceTest {

    static Recipe recipe(String name, String... ingredients) {
        Recipe r = new Recipe();
        r.setName(name);
        for (String i : ingredients) r.getIngredients().add(new RecipeIngredient(i, null));
        return r;
    }

    @Test
    void recipesUsingExpiringItemsRankFirst() {
        List<FoodItem> pantry = List.of(item("Milk", 1), item("Bananas", 0), item("Rice", 200), item("Old eggs", -1));
        List<RecipeMatch> ranked = RecipeService.rank(List.of(
                recipe("Rice bowl", "rice"),
                recipe("Banana pancakes", "banana", "egg", "milk"),
                recipe("Omelette", "egg")), pantry, TODAY);

        RecipeMatch best = ranked.get(0);
        assertEquals("Banana pancakes", best.recipe().getName());
        assertEquals(2, best.expiringUsed());
        assertEquals(List.of("egg"), best.missing());          // the eggs are expired, so they don't count
        assertEquals("Omelette", ranked.get(2).recipe().getName());
    }
}

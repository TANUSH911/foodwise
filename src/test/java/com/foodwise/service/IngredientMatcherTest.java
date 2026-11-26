package com.foodwise.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientMatcherTest {

    @Test
    void matchesIgnoringCaseAndPlurals() {
        assertTrue(IngredientMatcher.matches("Tomatoes", "tomato"));
        assertTrue(IngredientMatcher.matches("Eggs", "egg"));
        assertTrue(IngredientMatcher.matches("Green peas", "pea"));
        assertTrue(IngredientMatcher.matches("Chicken breast", "chicken"));
        assertTrue(IngredientMatcher.matches("Lentils (toor dal)", "lentil"));
    }

    @Test
    void doesNotMatchDifferentFoods() {
        assertFalse(IngredientMatcher.matches("Eggplant", "egg"));
        assertFalse(IngredientMatcher.matches("Onions", "spring onion"));
        assertFalse(IngredientMatcher.matches("Milk", ""));
    }
}

package com.foodwise.service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Decides whether a pantry item can stand in for a recipe ingredient.
 * Rule: every word of the ingredient must appear in the pantry item's name,
 * ignoring case and plurals. "Chicken breast" matches "chicken",
 * "Tomatoes" matches "tomato", but "Eggplant" does not match "egg".
 */
public final class IngredientMatcher {

    private IngredientMatcher() {
    }

    public static boolean matches(String pantryItemName, String ingredientName) {
        Set<String> ingredientWords = words(ingredientName);
        return !ingredientWords.isEmpty() && words(pantryItemName).containsAll(ingredientWords);
    }

    static Set<String> words(String text) {
        if (text == null) return Set.of();
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z]+"))
                .filter(w -> !w.isEmpty())
                .map(IngredientMatcher::singular)
                .collect(Collectors.toSet());
    }

    // shortcut: naive English plurals only; switch to a stemmer if recipes come from an external API.
    static String singular(String word) {
        int n = word.length();
        if (n > 4 && word.endsWith("oes")) return word.substring(0, n - 2);       // tomatoes -> tomato
        if (n > 4 && word.endsWith("ies")) return word.substring(0, n - 3) + "y"; // berries -> berry
        if (n > 3 && word.endsWith("s") && !word.endsWith("ss")) return word.substring(0, n - 1);
        return word;
    }
}

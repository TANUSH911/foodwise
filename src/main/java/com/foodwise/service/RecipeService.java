package com.foodwise.service;

import com.foodwise.entity.FoodItem;
import com.foodwise.entity.Recipe;
import com.foodwise.entity.RecipeIngredient;
import com.foodwise.entity.Urgency;
import com.foodwise.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Unique feature, part 2: ranks recipes by how many soon-to-expire
 * ingredients they use, and "cooks" a recipe (marks its items consumed).
 */
@Service
public class RecipeService {

    private final RecipeRepository recipes;
    private final FoodItemService foodItems;

    public RecipeService(RecipeRepository recipes, FoodItemService foodItems) {
        this.recipes = recipes;
        this.foodItems = foodItems;
    }

    /** All recipes scored against the active pantry, best first. */
    @Transactional(readOnly = true)
    public List<RecipeMatch> suggest(LocalDate today) {
        return rank(recipes.findAll(), foodItems.findActive(), today);
    }

    /** Pure ranking logic (no database), so it is easy to unit test. */
    public static List<RecipeMatch> rank(List<Recipe> recipes, List<FoodItem> pantry, LocalDate today) {
        List<FoodItem> usable = pantry.stream()
                .filter(item -> item.daysLeft(today) >= 0)                 // never cook with expired food
                .sorted(Comparator.comparing(FoodItem::getExpiryDate))     // prefer the item expiring first
                .toList();
        return recipes.stream()
                .map(recipe -> score(recipe, usable, today))
                .sorted(Comparator.comparingInt(RecipeMatch::score).reversed()
                        .thenComparingInt(m -> m.missing().size())
                        .thenComparing(m -> m.recipe().getName()))
                .toList();
    }

    private static RecipeMatch score(Recipe recipe, List<FoodItem> usable, LocalDate today) {
        List<FoodItem> used = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        int score = 0;
        int expiring = 0;
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            Optional<FoodItem> match = usable.stream()
                    .filter(item -> !used.contains(item))
                    .filter(item -> IngredientMatcher.matches(item.getName(), ingredient.getName()))
                    .findFirst();
            if (match.isPresent()) {
                long days = match.get().daysLeft(today);
                used.add(match.get());
                score += PriorityService.urgencyWeight(days);
                if (days <= Urgency.SOON_DAYS) expiring++;
            } else {
                missing.add(ingredient.getName());
            }
        }
        return new RecipeMatch(recipe, List.copyOf(used), List.copyOf(missing), expiring, score);
    }

    /** "Cook this": every pantry item the recipe uses is marked consumed. */
    public void cook(RecipeMatch match) {
        foodItems.markConsumed(match.usedItems());
    }

    /** Undo for {@link #cook(RecipeMatch)}. */
    public void uncook(RecipeMatch match) {
        foodItems.restore(match.usedItems());
    }
}

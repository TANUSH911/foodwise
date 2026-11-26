package com.foodwise.service;

import com.foodwise.entity.FoodItem;
import com.foodwise.entity.Recipe;

import java.util.List;

/**
 * A recipe scored against the current pantry.
 *
 * @param usedItems     pantry items the recipe would use up
 * @param missing       ingredient names not found in the pantry
 * @param expiringUsed  how many of the used items expire within {@code Urgency.SOON_DAYS}
 * @param score         sum of {@link PriorityService#urgencyWeight(long)} of used items
 */
public record RecipeMatch(Recipe recipe, List<FoodItem> usedItems, List<String> missing,
                          int expiringUsed, int score) {

    public boolean usesPantry() {
        return !usedItems.isEmpty();
    }
}

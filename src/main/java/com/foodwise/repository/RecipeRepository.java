package com.foodwise.repository;

import com.foodwise.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

/** SQL access for {@code recipe} and {@code recipe_ingredient}. */
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}

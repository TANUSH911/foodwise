package com.foodwise.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** One row of {@code recipe_ingredient}. */
@Embeddable
public class RecipeIngredient {

    @Column(name = "ingredient_name", nullable = false, length = 60)
    private String name;

    @Column(length = 40)
    private String amount;

    protected RecipeIngredient() {
    }

    public RecipeIngredient(String name, String amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() { return name; }
    public String getAmount() { return amount; }
}

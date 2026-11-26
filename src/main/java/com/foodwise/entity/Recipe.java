package com.foodwise.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** A recipe from the local catalogue (seeded by V2__seed_recipes.sql). */
@Entity
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(length = 40)
    private String cuisine;

    private Integer prepMinutes;

    private Integer servings;

    @Column(length = 10)
    private String difficulty;

    @Column(length = 255)
    private String imageUrl;

    @Column(nullable = false, length = 2000)
    private String instructions;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_ingredient", joinColumns = @JoinColumn(name = "recipe_id"))
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public Integer getPrepMinutes() { return prepMinutes; }
    public void setPrepMinutes(Integer prepMinutes) { this.prepMinutes = prepMinutes; }
    public Integer getServings() { return servings; }
    public void setServings(Integer servings) { this.servings = servings; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
}

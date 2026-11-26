package com.foodwise.service;

import com.foodwise.entity.FoodItem;

/** An entry in the "Eat this first" list: the item, its score and a short reason. */
public record PriorityItem(FoodItem item, long daysLeft, int score, String reason) {
}

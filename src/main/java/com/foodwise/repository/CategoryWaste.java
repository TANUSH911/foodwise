package com.foodwise.repository;

import com.foodwise.entity.FoodCategory;

import java.math.BigDecimal;

/** One row of the "waste by category" query. */
public record CategoryWaste(FoodCategory category, Long items, BigDecimal cost) {

    public CategoryWaste {
        cost = cost == null ? BigDecimal.ZERO : cost;
    }
}

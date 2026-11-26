package com.foodwise.service;

import com.foodwise.repository.CategoryWaste;
import com.foodwise.repository.MonthlyWaste;

import java.math.BigDecimal;
import java.util.List;

/** Everything the Stats screen and dashboard need about waste. */
public record WasteStats(long wastedThisMonth,
                         BigDecimal costThisMonth,
                         long consumedTotal,
                         long wastedTotal,
                         List<MonthlyWaste> monthly,
                         List<CategoryWaste> byCategory,
                         String insight) {

    /** Share of finished items that were wasted, 0-100. */
    public int wastePercent() {
        long finished = consumedTotal + wastedTotal;
        return finished == 0 ? 0 : Math.round(100f * wastedTotal / finished);
    }
}

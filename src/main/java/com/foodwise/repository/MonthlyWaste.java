package com.foodwise.repository;

import java.math.BigDecimal;
import java.time.YearMonth;

/** One row of the "waste per month" query. */
public record MonthlyWaste(Integer year, Integer month, Long items, BigDecimal cost) {

    public MonthlyWaste {
        cost = cost == null ? BigDecimal.ZERO : cost; // SUM(price) is NULL when no item has a price
    }

    public YearMonth yearMonth() {
        return YearMonth.of(year, month);
    }
}

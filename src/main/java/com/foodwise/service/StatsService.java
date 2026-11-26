package com.foodwise.service;

import com.foodwise.entity.ItemStatus;
import com.foodwise.repository.CategoryWaste;
import com.foodwise.repository.FoodItemRepository;
import com.foodwise.repository.MonthlyWaste;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Monthly waste tracking and statistics. */
@Service
@Transactional(readOnly = true)
public class StatsService {

    /** Number of months shown in the charts (including the current one). */
    public static final int MONTHS = 6;

    private final FoodItemRepository repository;

    public StatsService(FoodItemRepository repository) {
        this.repository = repository;
    }

    public WasteStats wasteStats(LocalDate today) {
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate windowStart = monthStart.minusMonths(MONTHS - 1);
        List<CategoryWaste> byCategory = repository.categoryTotals(ItemStatus.WASTED, windowStart);
        BigDecimal cost = repository.sumPriceByStatusSince(ItemStatus.WASTED, monthStart);
        return new WasteStats(
                repository.countByStatusAndStatusDateGreaterThanEqual(ItemStatus.WASTED, monthStart),
                cost == null ? BigDecimal.ZERO : cost,
                repository.countByStatus(ItemStatus.CONSUMED),
                repository.countByStatus(ItemStatus.WASTED),
                fillMissingMonths(repository.monthlyTotals(ItemStatus.WASTED, windowStart), YearMonth.from(windowStart)),
                byCategory,
                insight(byCategory));
    }

    /** The SQL only returns months that had waste; charts need every month, so add zeros. */
    static List<MonthlyWaste> fillMissingMonths(List<MonthlyWaste> rows, YearMonth first) {
        List<MonthlyWaste> result = new ArrayList<>();
        for (int i = 0; i < MONTHS; i++) {
            YearMonth ym = first.plusMonths(i);
            result.add(rows.stream()
                    .filter(r -> r.yearMonth().equals(ym))
                    .findFirst()
                    .orElse(new MonthlyWaste(ym.getYear(), ym.getMonthValue(), 0L, BigDecimal.ZERO)));
        }
        return result;
    }

    // shortcut: rule-based tip from the top category; an ML predictor is the planned next version.
    static String insight(List<CategoryWaste> byCategory) {
        if (byCategory.isEmpty()) {
            return "Nothing wasted in the last " + MONTHS + " months. Great job!";
        }
        CategoryWaste top = byCategory.get(0);
        return "You waste the most " + top.category().getLabel().toLowerCase()
                + " (" + top.items() + (top.items() == 1 ? " item" : " items")
                + "). Try buying smaller quantities or planning a meal around it.";
    }
}

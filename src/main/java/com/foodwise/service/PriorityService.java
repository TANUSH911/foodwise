package com.foodwise.service;

import com.foodwise.entity.FoodItem;
import com.foodwise.entity.Urgency;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Unique feature, part 1: ranks pantry items so the ones that will spoil
 * soonest are eaten first. Also supplies the urgency weight used to rank recipes.
 */
@Service
public class PriorityService {

    /**
     * How much an item "counts" when it is used up. Expiring sooner = heavier.
     * Expired items weigh 0: they should be checked, not cooked.
     */
    public static int urgencyWeight(long daysLeft) {
        if (daysLeft < 0) return 0;
        if (daysLeft == 0) return 10;
        if (daysLeft == 1) return 8;
        if (daysLeft == 2) return 6;
        if (daysLeft <= Urgency.SOON_DAYS) return 4;
        if (daysLeft <= 7) return 2;
        return 1;
    }

    /** Non-expired items, highest priority first. */
    public List<PriorityItem> eatFirst(List<FoodItem> activeItems, LocalDate today, int limit) {
        return activeItems.stream()
                .filter(item -> item.daysLeft(today) >= 0)
                .map(item -> toPriority(item, today))
                .sorted(Comparator.comparingInt(PriorityItem::score).reversed()
                        .thenComparingLong(PriorityItem::daysLeft)
                        .thenComparing(p -> p.item().getName()))
                .limit(limit)
                .toList();
    }

    private PriorityItem toPriority(FoodItem item, LocalDate today) {
        long days = item.daysLeft(today);
        String reason = Urgency.describe(days) + " · " + Formats.quantity(item) + " left";
        return new PriorityItem(item, days, urgencyWeight(days), reason);
    }
}

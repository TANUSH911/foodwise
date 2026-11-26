package com.foodwise.service;

import com.foodwise.entity.FoodItem;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Shared display formatting (money in rupees, quantities without trailing zeros). */
public final class Formats {

    private static final Locale INDIA = Locale.forLanguageTag("en-IN");

    private Formats() {
    }

    public static String money(BigDecimal amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(INDIA);
        format.setMaximumFractionDigits(0);
        return format.format(amount == null ? BigDecimal.ZERO : amount);
    }

    public static String quantity(FoodItem item) {
        return item.getQuantity().stripTrailingZeros().toPlainString() + " " + item.getUnit();
    }
}

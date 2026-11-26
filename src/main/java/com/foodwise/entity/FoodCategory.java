package com.foodwise.entity;

/**
 * Food categories. Stored in SQL as the enum name (e.g. "DAIRY").
 * {@code defaultShelfDays} pre-fills the expiry date in the add-item form.
 */
public enum FoodCategory {
    DAIRY("Dairy & Eggs", 7),
    VEGETABLES("Vegetables", 6),
    FRUITS("Fruits", 6),
    MEAT("Meat & Fish", 2),
    BAKERY("Bakery", 4),
    GRAINS("Grains & Pulses", 180),
    FROZEN("Frozen", 90),
    OTHER("Other", 14);

    private final String label;
    private final int defaultShelfDays;

    FoodCategory(String label, int defaultShelfDays) {
        this.label = label;
        this.defaultShelfDays = defaultShelfDays;
    }

    public String getLabel() {
        return label;
    }

    public int getDefaultShelfDays() {
        return defaultShelfDays;
    }
}

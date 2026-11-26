package com.foodwise.entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Expiry urgency, computed from the expiry date (never stored).
 * Declared most-urgent first, so sorting by ordinal puts EXPIRED on top.
 */
public enum Urgency {
    EXPIRED("Expired"),
    USE_TODAY("Use today"),
    USE_SOON("Use soon"),
    FRESH("Fresh");

    /** "Use soon" means expiring within this many days. */
    public static final int SOON_DAYS = 3;

    private final String label;

    Urgency(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** CSS class used by the theme, e.g. "urgency-use-soon". */
    public String cssClass() {
        return "urgency-" + name().toLowerCase().replace('_', '-');
    }

    public static long daysLeft(LocalDate expiryDate, LocalDate today) {
        return ChronoUnit.DAYS.between(today, expiryDate);
    }

    public static Urgency of(LocalDate expiryDate, LocalDate today) {
        long days = daysLeft(expiryDate, today);
        if (days < 0) return EXPIRED;
        if (days == 0) return USE_TODAY;
        if (days <= SOON_DAYS) return USE_SOON;
        return FRESH;
    }

    /** Human text such as "Expires tomorrow" or "Expired 2 days ago". */
    public static String describe(long daysLeft) {
        if (daysLeft < -1) return "Expired " + (-daysLeft) + " days ago";
        if (daysLeft == -1) return "Expired yesterday";
        if (daysLeft == 0) return "Expires today";
        if (daysLeft == 1) return "Expires tomorrow";
        return "Expires in " + daysLeft + " days";
    }
}

package com.foodwise.view;

/** Views that reload their data when something changes elsewhere (e.g. an item added from the nav bar). */
public interface Refreshable {
    void refresh();
}

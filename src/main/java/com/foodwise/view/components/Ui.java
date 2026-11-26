package com.foodwise.view.components;

import com.foodwise.entity.FoodCategory;
import com.foodwise.entity.Urgency;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/** Small UI building blocks shared by all screens. Styles live in themes/foodwise/styles.css. */
public final class Ui {

    private Ui() {
    }

    /**
     * A Lucide icon from src/main/resources/META-INF/resources/icons, drawn with a CSS
     * mask so it takes the current text colour. Decorative: hidden from screen readers.
     */
    public static Span icon(String name, String... sizeClass) {
        Span icon = new Span();
        icon.addClassName("fw-icon");
        icon.addClassNames(sizeClass);
        String url = "url('icons/" + name + ".svg')";
        icon.getStyle().set("mask-image", url).set("-webkit-mask-image", url);
        icon.getElement().setAttribute("aria-hidden", "true");
        return icon;
    }

    public static Span categoryIcon(FoodCategory category, String... sizeClass) {
        return icon(switch (category) {
            case DAIRY -> "milk";
            case VEGETABLES -> "carrot";
            case FRUITS -> "apple";
            case MEAT -> "beef";
            case BAKERY -> "croissant";
            case GRAINS -> "wheat";
            case FROZEN -> "snowflake";
            case OTHER -> "package";
        }, sizeClass);
    }

    public static Span urgencyBadge(Urgency urgency) {
        Span badge = new Span(urgency.getLabel());
        badge.addClassNames("fw-badge", urgency.cssClass());
        return badge;
    }

    /** Card with an icon, a big number and a label. */
    public static Div statCard(String iconName, String value, String label) {
        Div iconBox = new Div(icon(iconName));
        iconBox.addClassName("fw-stat-icon");
        Span valueSpan = new Span(value);
        valueSpan.addClassName("fw-stat-value");
        Span labelSpan = new Span(label);
        labelSpan.addClassName("fw-stat-label");
        Div card = new Div(iconBox, valueSpan, labelSpan);
        card.addClassNames("fw-card", "fw-stat");
        return card;
    }

    public static Div card(Component... children) {
        Div card = new Div(children);
        card.addClassName("fw-card");
        return card;
    }

    public static Div emptyState(String iconName, String title, String text) {
        Div avatar = new Div(icon(iconName, "l"));
        avatar.addClassName("fw-avatar");
        Span heading = new Span(title);
        heading.getStyle().set("font-weight", "700").set("color", "var(--fw-ink)");
        Div empty = new Div(avatar, heading, new Span(text));
        empty.addClassNames("fw-card", "fw-empty");
        return empty;
    }

    public static void toast(String message) {
        Notification n = Notification.show(message, 3500, Notification.Position.TOP_END);
        n.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
    }

    /** Toast with an Undo button, for one-tap actions that change the waste stats. */
    public static void undoToast(String message, Runnable undo) {
        Notification n = new Notification();
        n.setDuration(8000);
        n.setPosition(Notification.Position.TOP_END);
        n.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        Button undoButton = new Button("Undo", e -> {
            undo.run();
            n.close();
        });
        undoButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        undoButton.getStyle().set("color", "var(--fw-accent)").set("font-weight", "700");
        Div row = new Div(new Span(message), undoButton);
        row.getStyle().set("display", "flex").set("align-items", "center").set("gap", "16px");
        n.add(row);
        n.open();
    }

    public static void warningToast(String message) {
        Notification n = Notification.show(message, 5000, Notification.Position.TOP_END);
        n.addThemeVariants(NotificationVariant.LUMO_WARNING);
    }
}

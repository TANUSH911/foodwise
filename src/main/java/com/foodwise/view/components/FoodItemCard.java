package com.foodwise.view.components;

import com.foodwise.entity.FoodItem;
import com.foodwise.entity.Urgency;
import com.foodwise.service.Formats;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import java.time.LocalDate;
import java.util.function.Consumer;

/** Colour-coded pantry item card with Eaten / Wasted / Edit / Delete actions. */
public class FoodItemCard extends Div {

    /** Callbacks for the card's buttons. A null edit/delete hides that button. */
    public record Actions(Consumer<FoodItem> consumed, Consumer<FoodItem> wasted,
                          Consumer<FoodItem> edit, Consumer<FoodItem> delete) {
    }

    public FoodItemCard(FoodItem item, LocalDate today, Actions actions) {
        Urgency urgency = item.urgency(today);
        addClassNames("fw-card", "fw-item", urgency.cssClass());

        Div avatar = new Div(Ui.categoryIcon(item.getCategory()));
        avatar.addClassName("fw-avatar");
        Span name = new Span(item.getName());
        name.addClassName("fw-item-name");
        Span meta = new Span(Formats.quantity(item) + " · " + item.getCategory().getLabel());
        meta.addClassName("fw-item-meta");
        Div text = new Div(name, meta);
        text.addClassName("fw-item-text");
        Div main = new Div(avatar, text, Ui.urgencyBadge(urgency));
        main.addClassName("fw-item-main");

        Span expiry = new Span(Ui.icon("clock", "s"), new Span(Urgency.describe(item.daysLeft(today))));
        expiry.addClassName("fw-item-expiry");
        Div buttons = new Div();
        buttons.addClassName("fw-item-actions");
        buttons.add(button("Eaten", "check", "Mark " + item.getName() + " as eaten", () -> actions.consumed().accept(item)));
        buttons.add(button("Wasted", "trash-2", "Mark " + item.getName() + " as wasted", () -> actions.wasted().accept(item)));
        if (actions.edit() != null) {
            buttons.add(button(null, "pencil", "Edit " + item.getName(), () -> actions.edit().accept(item)));
        }
        if (actions.delete() != null) {
            buttons.add(button(null, "x", "Delete " + item.getName(), () -> actions.delete().accept(item)));
        }
        Div foot = new Div(expiry, buttons);
        foot.addClassName("fw-item-foot");

        add(main, foot);
    }

    static Button button(String text, String iconName, String ariaLabel, Runnable onClick) {
        Button button = new Button(text, (Component) Ui.icon(iconName, "s"), e -> onClick.run());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        button.setAriaLabel(ariaLabel);
        button.setTooltipText(ariaLabel);
        if (text == null) {
            button.addThemeVariants(ButtonVariant.LUMO_ICON);
        }
        return button;
    }
}

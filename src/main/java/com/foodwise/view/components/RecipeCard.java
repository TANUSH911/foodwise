package com.foodwise.view.components;

import com.foodwise.entity.Recipe;
import com.foodwise.service.RecipeMatch;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

/** Recipe tile: photo, time, "uses N expiring items", difficulty and what's missing. Opens the detail dialog. */
public class RecipeCard extends Div {

    public RecipeCard(RecipeMatch match, Runnable onOpen) {
        Recipe recipe = match.recipe();
        addClassNames("fw-card", "fw-recipe");
        getElement().setAttribute("role", "button");
        getElement().setAttribute("tabindex", "0");
        getElement().setAttribute("aria-label", "Open recipe " + recipe.getName());
        addClickListener(e -> onOpen.run());
        getElement().addEventListener("keydown", e -> onOpen.run()).setFilter("event.key === 'Enter'");

        Div photo = photo(recipe);
        if (recipe.getPrepMinutes() != null) {
            Span time = new Span(Ui.icon("clock", "s"), new Span(recipe.getPrepMinutes() + " min"));
            time.addClassName("fw-pill");
            photo.add(time);
        }
        if (match.expiringUsed() > 0) {
            Span uses = new Span(Ui.icon("flame", "s"), new Span("Uses " + match.expiringUsed() + " expiring"));
            uses.addClassNames("fw-pill", "accent");
            photo.add(uses);
        }

        Span name = new Span(recipe.getName());
        name.addClassName("fw-recipe-name");
        Span sub = new Span(recipe.getCuisine() + " · uses " + match.usedItems().size() + " of "
                + recipe.getIngredients().size() + " ingredients from your pantry");
        sub.addClassName("fw-recipe-sub");
        Div body = new Div(name, sub);
        body.addClassName("fw-recipe-body");

        Span missing = new Span(match.missing().isEmpty() ? "You have everything" : "Missing: " + String.join(", ", match.missing()));
        missing.addClassName("fw-missing");
        if (match.missing().isEmpty()) missing.addClassName("none");
        Div foot = new Div(difficulty(recipe.getDifficulty()), missing);
        foot.addClassName("fw-recipe-foot");

        add(photo, body, foot);
    }

    /** Photo with a gradient fallback when the image can't load (offline). */
    static Div photo(Recipe recipe) {
        Div photo = new Div();
        photo.addClassName("fw-recipe-photo");
        if (recipe.getImageUrl() != null) {
            Image image = new Image(recipe.getImageUrl(), recipe.getName());
            image.getElement().setAttribute("loading", "lazy");
            image.getElement().setAttribute("onerror", "this.remove()");
            photo.add(image);
        }
        return photo;
    }

    /** "Easy ▮▯▯" style level bars; the word is always shown so it isn't colour-only. */
    static Span difficulty(String level) {
        int bars = switch (level == null ? "" : level) {
            case "Hard" -> 3;
            case "Medium" -> 2;
            default -> 1;
        };
        Span wrap = new Span(new Span(level == null ? "Easy" : level));
        wrap.addClassName("fw-level");
        for (int i = 1; i <= 3; i++) {
            Span bar = new Span();
            bar.addClassName("fw-bar");
            bar.getElement().setAttribute("aria-hidden", "true");
            if (i <= bars) bar.addClassName("on");
            wrap.add(bar);
        }
        return wrap;
    }
}

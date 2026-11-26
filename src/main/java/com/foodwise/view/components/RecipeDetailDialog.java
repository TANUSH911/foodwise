package com.foodwise.view.components;

import com.foodwise.entity.FoodItem;
import com.foodwise.entity.Recipe;
import com.foodwise.entity.RecipeIngredient;
import com.foodwise.entity.Urgency;
import com.foodwise.service.IngredientMatcher;
import com.foodwise.service.RecipeMatch;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

/** Full recipe: photo, facts, ingredients (have / missing), directions and the "Cook this" button. */
public class RecipeDetailDialog extends Dialog {

    public RecipeDetailDialog(RecipeMatch match, LocalDate today, Runnable onCook) {
        Recipe recipe = match.recipe();
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        setWidth("min(640px, 96vw)");
        getElement().setAttribute("aria-label", recipe.getName());

        Div photo = RecipeCard.photo(recipe);
        photo.removeClassName("fw-recipe-photo");
        photo.addClassName("fw-detail-photo");
        Button close = new Button(Ui.icon("x"), e -> close());
        close.addClassName("fw-detail-close");
        close.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_CONTRAST);
        close.setAriaLabel("Close");
        photo.add(close);

        H2 title = new H2(recipe.getName());
        Span cuisine = new Span(recipe.getCuisine());
        cuisine.addClassName("fw-recipe-sub");

        Div facts = new Div(
                fact(recipe.getPrepMinutes() == null ? "-" : recipe.getPrepMinutes() + " min", "Time"),
                fact(recipe.getServings() == null ? "-" : String.valueOf(recipe.getServings()), "Servings"),
                fact(recipe.getDifficulty() == null ? "Easy" : recipe.getDifficulty(), "Difficulty"));
        facts.addClassName("fw-detail-facts");

        UnorderedList ingredients = new UnorderedList();
        ingredients.addClassName("fw-ingredients");
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            Optional<FoodItem> have = match.usedItems().stream()
                    .filter(item -> IngredientMatcher.matches(item.getName(), ingredient.getName()))
                    .findFirst();
            String label = capitalize(ingredient.getName()) + (ingredient.getAmount() == null ? "" : " · " + ingredient.getAmount());
            Span note = new Span(have.map(item -> "In pantry · " + Urgency.describe(item.daysLeft(today)).toLowerCase())
                    .orElse("Missing"));
            note.addClassName("fw-note");
            ListItem li = new ListItem(Ui.icon(have.isPresent() ? "check" : "x", "s"), new Span(label), note);
            li.addClassName(have.isPresent() ? "have" : "miss");
            ingredients.add(li);
        }

        Div body = new Div(new Div(title, cuisine), facts);
        if (match.expiringUsed() > 0) {
            Span callout = new Span(Ui.icon("flame", "s"), new Span("Rescues " + match.expiringUsed()
                    + " item(s) that expire soon: " + match.usedItems().stream()
                    .filter(i -> i.daysLeft(today) <= Urgency.SOON_DAYS)
                    .map(FoodItem::getName).collect(Collectors.joining(", "))));
            callout.addClassNames("fw-pill", "accent");
            callout.getStyle().set("align-self", "flex-start").set("white-space", "normal");
            body.add(callout);
        }
        body.add(new H3("Ingredients"), ingredients,
                new Paragraph("Plus salt, oil and spices from your shelf."),
                new H3("Directions"), new Paragraph(recipe.getInstructions()));
        body.addClassName("fw-detail-body");
        add(photo, body);

        Button cancel = new Button("Close", e -> close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Button cook = new Button("Cook this", Ui.icon("chef-hat", "s"), e -> {
            onCook.run();
            close();
        });
        cook.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        if (match.usedItems().isEmpty()) {
            cook.setEnabled(false);
            cook.setTooltipText("None of the ingredients are in your pantry");
        } else {
            cook.setTooltipText("Marks " + match.usedItems().size() + " pantry item(s) as eaten");
        }
        getFooter().add(cancel, cook);
    }

    private static Div fact(String value, String label) {
        Span strong = new Span(value);
        strong.addClassName("fw-fact-value");
        Div fact = new Div(strong, new Span(label));
        fact.addClassName("fw-fact");
        return fact;
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

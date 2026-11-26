package com.foodwise.view;

import com.foodwise.service.RecipeMatch;
import com.foodwise.service.RecipeService;
import com.foodwise.view.components.RecipeCard;
import com.foodwise.view.components.RecipeDetailDialog;
import com.foodwise.view.components.Ui;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/** Recipe suggestions ranked by how many soon-to-expire ingredients they rescue. */
@Route(value = "recipes", layout = MainLayout.class)
@PageTitle("Recipes · FoodWise")
public class RecipesView extends Div implements Refreshable {

    private enum Filter {
        ALL("All recipes", m -> true),
        EXPIRING("Uses expiring food", m -> m.expiringUsed() > 0),
        READY("Nothing missing", m -> m.missing().isEmpty());

        final String label;
        final Predicate<RecipeMatch> test;

        Filter(String label, Predicate<RecipeMatch> test) {
            this.label = label;
            this.test = test;
        }
    }

    private final RecipeService recipes;
    private final TextField search = new TextField();
    private final Div chips = new Div();
    private final Div grid = new Div();
    private List<RecipeMatch> matches = List.of();
    private Filter filter = Filter.ALL;

    public RecipesView(RecipeService recipes) {
        this.recipes = recipes;
        addClassName("fw-page");

        Paragraph sub = new Paragraph("Ranked by how many of your soon-to-expire ingredients each recipe uses.");
        sub.addClassName("fw-subtitle");
        Div head = new Div(new Div(new H1("Recipes"), sub));
        head.addClassName("fw-page-head");

        search.setPlaceholder("Search any recipe");
        search.setPrefixComponent(Ui.icon("search", "s"));
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> render());
        search.addClassName("fw-search");
        search.getElement().setProperty("accessibleName", "Search recipes");
        chips.addClassName("fw-chips");
        Div toolbar = new Div(search, chips);
        toolbar.addClassName("fw-toolbar");

        grid.addClassName("fw-card-grid");
        add(head, toolbar, grid);
        refresh();
    }

    @Override
    public void refresh() {
        matches = recipes.suggest(LocalDate.now());
        render();
    }

    private void render() {
        LocalDate today = LocalDate.now();
        chips.removeAll();
        for (Filter f : Filter.values()) {
            NativeButton chip = new NativeButton();
            chip.addClassName("fw-chip");
            Span count = new Span(String.valueOf(matches.stream().filter(f.test).count()));
            count.addClassName("fw-chip-count");
            chip.add(new Span(f.label), count);
            chip.getElement().setAttribute("aria-pressed", String.valueOf(f == filter));
            chip.addClickListener(e -> {
                filter = f;
                render();
            });
            chips.add(chip);
        }

        String query = search.getValue() == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        List<RecipeMatch> visible = matches.stream()
                .filter(filter.test)
                .filter(m -> query.isEmpty() || m.recipe().getName().toLowerCase(Locale.ROOT).contains(query)
                        || m.recipe().getIngredients().stream().anyMatch(i -> i.getName().contains(query)))
                .toList();

        grid.removeAll();
        if (visible.isEmpty()) {
            grid.add(Ui.emptyState("chef-hat", "No recipes found", "Try another search or filter."));
        }
        for (RecipeMatch match : visible) {
            grid.add(new RecipeCard(match, () -> new RecipeDetailDialog(match, today, () -> {
                recipes.cook(match);
                refresh();
                Ui.undoToast("Enjoy! " + match.usedItems().size() + " item(s) marked as eaten", () -> {
                    recipes.uncook(match);
                    refresh();
                });
            }).open()));
        }
    }
}

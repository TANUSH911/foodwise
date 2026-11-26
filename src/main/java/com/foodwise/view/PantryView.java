package com.foodwise.view;

import com.foodwise.entity.FoodCategory;
import com.foodwise.entity.FoodItem;
import com.foodwise.entity.Urgency;
import com.foodwise.service.FoodItemService;
import com.foodwise.view.components.FoodItemCard;
import com.foodwise.view.components.ItemFormDialog;
import com.foodwise.view.components.Ui;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Pantry list: search, filter by category and urgency, colour-coded cards, most urgent first. */
@Route(value = "pantry", layout = MainLayout.class)
@PageTitle("Pantry · FoodWise")
public class PantryView extends Div implements Refreshable {

    private final FoodItemService foodItems;
    private final TextField search = new TextField();
    private final Div categoryChips = new Div();
    private final Div urgencyChips = new Div();
    private final Div cards = new Div();
    private final Paragraph subtitle = new Paragraph();

    private List<FoodItem> items = List.of();
    private FoodCategory categoryFilter;   // null = all
    private Urgency urgencyFilter;         // null = all

    public PantryView(FoodItemService foodItems) {
        this.foodItems = foodItems;
        addClassName("fw-page");

        subtitle.addClassName("fw-subtitle");
        Button add = new Button("Add item", Ui.icon("plus", "s"), e -> openForm(new FoodItem()));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Div head = new Div(new Div(new H1("Pantry"), subtitle), add);
        head.addClassName("fw-page-head");

        search.setPlaceholder("Search your pantry");
        search.setPrefixComponent(Ui.icon("search", "s"));
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> render());
        search.addClassName("fw-search");
        search.getElement().setProperty("accessibleName", "Search your pantry");
        categoryChips.addClassName("fw-chips");
        categoryChips.getElement().setAttribute("aria-label", "Filter by category");
        urgencyChips.addClassName("fw-chips");
        urgencyChips.getElement().setAttribute("aria-label", "Filter by urgency");
        Div toolbar = new Div(search, categoryChips, urgencyChips);
        toolbar.addClassName("fw-toolbar");

        cards.addClassName("fw-card-grid");
        add(head, toolbar, cards);
        refresh();
    }

    @Override
    public void refresh() {
        items = foodItems.findActive();
        render();
    }

    private void render() {
        LocalDate today = LocalDate.now();
        subtitle.setText(items.size() + " items · most urgent first");
        renderChips(today);

        String query = search.getValue() == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        List<FoodItem> visible = items.stream()
                .filter(i -> categoryFilter == null || i.getCategory() == categoryFilter)
                .filter(i -> urgencyFilter == null || i.urgency(today) == urgencyFilter)
                .filter(i -> query.isEmpty() || i.getName().toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparing((FoodItem i) -> i.urgency(today)).thenComparing(FoodItem::getExpiryDate))
                .toList();

        cards.removeAll();
        if (visible.isEmpty()) {
            cards.add(items.isEmpty()
                    ? Ui.emptyState("refrigerator", "Your pantry is empty", "Add your groceries with the + button.")
                    : Ui.emptyState("search", "No matches", "Try another search or clear the filters."));
            return;
        }
        FoodItemCard.Actions actions = new FoodItemCard.Actions(
                item -> { foodItems.markConsumed(item); undoable(item.getName() + " marked as eaten", item); },
                item -> { foodItems.markWasted(item); undoable(item.getName() + " marked as wasted", item); },
                this::openForm,
                this::confirmDelete);
        visible.forEach(item -> cards.add(new FoodItemCard(item, today, actions)));
    }

    private void undoable(String message, FoodItem item) {
        refresh();
        Ui.undoToast(message, () -> {
            foodItems.restore(List.of(item));
            refresh();
        });
    }

    private void renderChips(LocalDate today) {
        categoryChips.removeAll();
        categoryChips.add(chip(null, "All", null, categoryFilter == null, () -> categoryFilter = null));
        for (FoodCategory c : FoodCategory.values()) {
            long n = items.stream().filter(i -> i.getCategory() == c).count();
            if (n > 0) {
                categoryChips.add(chip(Ui.categoryIcon(c, "s"), c.getLabel(), n, c == categoryFilter, () -> categoryFilter = c));
            }
        }
        urgencyChips.removeAll();
        urgencyChips.add(chip(null, "Any urgency", null, urgencyFilter == null, () -> urgencyFilter = null));
        for (Urgency u : Urgency.values()) {
            long n = items.stream().filter(i -> i.urgency(today) == u).count();
            Span dot = new Span();
            dot.addClassNames("fw-dot");
            dot.getStyle().set("background", "var(--fw-" + switch (u) {
                case EXPIRED -> "expired";
                case USE_TODAY -> "today";
                case USE_SOON -> "soon";
                case FRESH -> "fresh";
            } + ")");
            urgencyChips.add(chip(dot, u.getLabel(), n, u == urgencyFilter, () -> urgencyFilter = u));
        }
    }

    private NativeButton chip(Component icon, String label, Long count, boolean pressed, Runnable select) {
        NativeButton chip = new NativeButton();
        chip.addClassName("fw-chip");
        if (icon != null) chip.add(icon);
        chip.add(new Span(label));
        if (count != null) {
            Span c = new Span(String.valueOf(count));
            c.addClassName("fw-chip-count");
            chip.add(c);
        }
        chip.getElement().setAttribute("aria-pressed", String.valueOf(pressed));
        chip.addClickListener(e -> {
            select.run();
            render();
        });
        return chip;
    }

    private void openForm(FoodItem item) {
        new ItemFormDialog(item, edited -> {
            foodItems.save(edited);
            Ui.toast("Saved " + edited.getName());
            refresh();
        }).open();
    }

    private void confirmDelete(FoodItem item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete " + item.getName() + "?");
        dialog.add(new Paragraph("Delete is for mistakes. If the food went bad, mark it as Wasted instead so it counts in your stats."));
        dialog.setWidth("min(440px, 96vw)");
        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Button delete = new Button("Delete", e -> {
            foodItems.delete(item);
            dialog.close();
            Ui.toast(item.getName() + " deleted");
            refresh();
        });
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(cancel, delete);
        dialog.open();
    }
}

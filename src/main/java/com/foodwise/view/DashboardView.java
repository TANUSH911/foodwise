package com.foodwise.view;

import com.foodwise.entity.FoodItem;
import com.foodwise.entity.Urgency;
import com.foodwise.service.FoodItemService;
import com.foodwise.service.Formats;
import com.foodwise.service.PriorityItem;
import com.foodwise.service.PriorityService;
import com.foodwise.service.RecipeMatch;
import com.foodwise.service.RecipeService;
import com.foodwise.service.StatsService;
import com.foodwise.service.WasteStats;
import com.foodwise.view.components.RecipeCard;
import com.foodwise.view.components.RecipeDetailDialog;
import com.foodwise.view.components.Ui;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Home screen: freshness gauge, summary cards, "Use these first" and recipe picks. */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard · FoodWise")
public class DashboardView extends Div implements Refreshable {

    private static final int GAUGE_SEGMENTS = 11;

    private final FoodItemService foodItems;
    private final PriorityService priority;
    private final RecipeService recipes;
    private final StatsService stats;

    public DashboardView(FoodItemService foodItems, PriorityService priority, RecipeService recipes, StatsService stats) {
        this.foodItems = foodItems;
        this.priority = priority;
        this.recipes = recipes;
        this.stats = stats;
        addClassName("fw-page");
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();
        LocalDate today = LocalDate.now();
        List<FoodItem> active = foodItems.findActive();
        WasteStats waste = stats.wasteStats(today);
        long expired = active.stream().filter(i -> i.urgency(today) == Urgency.EXPIRED).count();
        long expiringSoon = active.stream()
                .filter(i -> i.urgency(today) == Urgency.USE_TODAY || i.urgency(today) == Urgency.USE_SOON).count();
        long fresh = active.stream().filter(i -> i.urgency(today) == Urgency.FRESH).count();

        Paragraph eyebrow = new Paragraph(greeting() + " · " + today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")));
        eyebrow.addClassName("fw-eyebrow");
        Div head = new Div(new Div(eyebrow, new H1("Your kitchen today")));
        head.addClassName("fw-page-head");

        Div statCards = new Div(
                Ui.statCard("clock", String.valueOf(expiringSoon), "Expiring in " + Urgency.SOON_DAYS + " days"),
                Ui.statCard("trash-2", String.valueOf(expired), "Already expired"),
                Ui.statCard("chart-column", Formats.money(waste.costThisMonth()), "Wasted this month"));
        statCards.addClassName("fw-stats");

        Div left = new Div(gauge(fresh, active.size(), expiringSoon), statCards);
        left.addClassName("fw-stack");

        Div dash = new Div(left, useFirst(today, active));
        dash.addClassName("fw-dash");

        add(head, dash, recipePicks(today));
    }

    /** Segmented half-circle: share of pantry items that are still fresh. */
    private Component gauge(long fresh, int total, long expiringSoon) {
        int percent = total == 0 ? 100 : Math.round(100f * fresh / total);
        int on = Math.round(GAUGE_SEGMENTS * percent / 100f);
        Div gauge = new Div();
        gauge.addClassName("fw-gauge");
        gauge.getElement().setAttribute("role", "img");
        gauge.getElement().setAttribute("aria-label", percent + "% of your pantry is fresh");
        for (int i = 0; i < GAUGE_SEGMENTS; i++) {
            double angle = -90 + i * (180.0 / (GAUGE_SEGMENTS - 1));
            Div seg = new Div();
            seg.addClassName("fw-gauge-seg");
            if (i < on) seg.addClassName("on");
            seg.getStyle().set("--a", angle + "deg"); // geometry in styles.css (.fw-gauge-seg)
            gauge.add(seg);
        }
        Span value = new Span(percent + "%");
        value.addClassName("fw-gauge-value");
        Span label = new Span(fresh + " of " + total + " items fresh");
        label.addClassName("fw-gauge-label");
        Div center = new Div(Ui.icon("leaf", "s"), value, label);
        center.addClassName("fw-gauge-center");
        center.getElement().setAttribute("aria-hidden", "true");
        gauge.add(center);

        Span note = new Span(expiringSoon == 0 ? "Nothing urgent. Nice!" : expiringSoon + " item(s) need eating soon");
        note.addClassName("fw-gauge-note");
        Div card = Ui.card(gauge, note);
        card.addClassName("fw-gauge-card");
        return card;
    }

    /** Unique feature: the top 5 items to eat first, with a reason each. */
    private Component useFirst(LocalDate today, List<FoodItem> active) {
        List<PriorityItem> top = priority.eatFirst(active, today, 5);
        H2 title = new H2("Use these first");
        Span count = new Span(String.valueOf(top.size()));
        count.addClassName("fw-count-pill");
        title.add(count);
        Div header = new Div(title, new RouterLink("See pantry", PantryView.class));
        header.addClassName("fw-section-head");

        Div list = new Div();
        list.addClassName("fw-stack");
        list.getStyle().set("gap", "10px");
        if (top.isEmpty()) {
            list.add(Ui.emptyState("refrigerator", "Your pantry is empty", "Add food with the + button to get started."));
        }
        int rank = 1;
        for (PriorityItem p : top) {
            list.add(priorityRow(rank++, p, today));
        }
        Div section = new Div(header, list);
        section.addClassName("fw-stack");
        return section;
    }

    private Component priorityRow(int rank, PriorityItem p, LocalDate today) {
        FoodItem item = p.item();
        Span rankBadge = new Span(String.valueOf(rank));
        rankBadge.addClassName("fw-rank");
        Div avatar = new Div(Ui.categoryIcon(item.getCategory()));
        avatar.addClassName("fw-avatar");
        Span name = new Span(item.getName());
        name.addClassName("fw-item-name");
        Span reason = new Span(p.reason());
        reason.addClassName("fw-item-meta");
        Div text = new Div(name, reason);
        text.addClassName("fw-item-text");

        Button eaten = iconButton("check", "Mark " + item.getName() + " as eaten", () -> {
            foodItems.markConsumed(item);
            undoable(item.getName() + " marked as eaten", List.of(item));
        });
        Button wasted = iconButton("trash-2", "Mark " + item.getName() + " as wasted", () -> {
            foodItems.markWasted(item);
            undoable(item.getName() + " marked as wasted", List.of(item));
        });
        Div actions = new Div(Ui.urgencyBadge(item.urgency(today)), eaten, wasted);
        actions.addClassName("fw-item-actions");
        actions.getStyle().set("align-items", "center");

        Div row = new Div(rankBadge, avatar, text, actions);
        row.addClassNames("fw-card", "fw-priority");
        return row;
    }

    private void undoable(String message, List<FoodItem> items) {
        refresh();
        Ui.undoToast(message, () -> {
            foodItems.restore(items);
            refresh();
        });
    }

    private static Button iconButton(String icon, String label, Runnable action) {
        Button button = new Button(Ui.icon(icon, "s"), e -> action.run());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        button.setAriaLabel(label);
        button.setTooltipText(label);
        return button;
    }

    private Component recipePicks(LocalDate today) {
        List<RecipeMatch> picks = recipes.suggest(today).stream().filter(RecipeMatch::usesPantry).limit(3).toList();
        H2 title = new H2("Cook before it spoils");
        Div header = new Div(title, new RouterLink("All recipes", RecipesView.class));
        header.addClassName("fw-section-head");
        Div grid = new Div();
        grid.addClassName("fw-card-grid");
        for (RecipeMatch match : picks) {
            grid.add(new RecipeCard(match, () -> new RecipeDetailDialog(match, today, () -> {
                recipes.cook(match);
                undoable("Enjoy! " + match.usedItems().size() + " item(s) marked as eaten", match.usedItems());
            }).open()));
        }
        Div section = new Div(header, picks.isEmpty()
                ? Ui.emptyState("chef-hat", "No matches yet", "Add a few ingredients and we'll suggest recipes.")
                : grid);
        section.addClassName("fw-stack");
        return section;
    }

    private static String greeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }
}

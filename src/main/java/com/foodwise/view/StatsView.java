package com.foodwise.view;

import com.foodwise.repository.CategoryWaste;
import com.foodwise.repository.MonthlyWaste;
import com.foodwise.service.Formats;
import com.foodwise.service.StatsService;
import com.foodwise.service.WasteStats;
import com.foodwise.view.components.Ui;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Waste statistics. Charts are plain HTML/CSS bars (no chart library needed):
 * one series per chart, values printed on the bars, tooltips via the title attribute.
 */
@Route(value = "stats", layout = MainLayout.class)
@PageTitle("Waste stats · FoodWise")
public class StatsView extends Div implements Refreshable {

    private final StatsService stats;

    public StatsView(StatsService stats) {
        this.stats = stats;
        addClassName("fw-page");
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();
        WasteStats s = stats.wasteStats(LocalDate.now());

        Paragraph sub = new Paragraph("Your food waste over the last " + StatsService.MONTHS + " months.");
        sub.addClassName("fw-subtitle");
        Div head = new Div(new Div(new H1("Waste stats"), sub));
        head.addClassName("fw-page-head");

        Div cards = new Div(
                Ui.statCard("trash-2", String.valueOf(s.wastedThisMonth()), "Items wasted this month"),
                Ui.statCard("chart-column", Formats.money(s.costThisMonth()), "Money wasted this month"),
                Ui.statCard("leaf", s.wastePercent() + "%", "Of finished food was wasted"));
        cards.addClassName("fw-stats");

        Div insightIcon = new Div(Ui.icon("sparkles"));
        insightIcon.addClassName("fw-stat-icon");
        Div insight = Ui.card(insightIcon, new Span(s.insight()));
        insight.addClassName("fw-insight");

        Div charts = new Div(monthlyChart(s), categoryChart(s));
        charts.addClassName("fw-chart-grid");

        add(head, cards, insight, charts, consumedVsWasted(s));
    }

    private Component monthlyChart(WasteStats s) {
        long max = Math.max(1, s.monthly().stream().mapToLong(MonthlyWaste::items).max().orElse(1));
        Div columns = new Div();
        columns.addClassName("fw-columns");
        for (MonthlyWaste m : s.monthly()) {
            String month = m.yearMonth().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            Div bar = new Div();
            bar.addClassName("fw-column-bar");
            bar.getStyle().set("height", "calc((100% - 48px) * " + m.items() + " / " + max + ")");
            Span value = new Span(String.valueOf(m.items()));
            value.addClassName("fw-column-value");
            Span label = new Span(month);
            label.addClassName("fw-column-label");
            Div column = new Div(value, bar, label);
            column.addClassName("fw-column");
            column.getElement().setAttribute("title",
                    month + " " + m.year() + ": " + m.items() + " item(s) wasted, " + Formats.money(m.cost()));
            columns.add(column);
        }
        return chartCard("Items wasted per month", "Hover a bar for the cost", columns);
    }

    private Component categoryChart(WasteStats s) {
        Div bars = new Div();
        bars.addClassName("fw-hbars");
        if (s.byCategory().isEmpty()) {
            bars.add(new Span("No waste recorded. Great job!"));
        }
        long max = s.byCategory().stream().mapToLong(CategoryWaste::items).max().orElse(1);
        for (CategoryWaste c : s.byCategory()) {
            Div fill = new Div();
            fill.addClassName("fw-hbar-fill");
            fill.getStyle().set("width", (100.0 * c.items() / max) + "%");
            Div track = new Div(fill);
            track.addClassName("fw-hbar-track");
            Span value = new Span(String.valueOf(c.items()));
            value.addClassName("fw-hbar-value");
            Div row = new Div(new Span(c.category().getLabel()), track, value);
            row.addClassName("fw-hbar");
            row.getElement().setAttribute("title", c.category().getLabel() + ": " + c.items()
                    + " item(s), " + Formats.money(c.cost()));
            bars.add(row);
        }
        return chartCard("Waste by category", "Number of items, last " + StatsService.MONTHS + " months", bars);
    }

    private Component consumedVsWasted(WasteStats s) {
        long total = s.consumedTotal() + s.wastedTotal();
        int wastedPct = s.wastePercent();
        Div consumed = new Div();
        consumed.getStyle().set("width", (total == 0 ? 0 : 100 - wastedPct) + "%").set("background", "var(--fw-chart-consumed)");
        Div wasted = new Div();
        wasted.getStyle().set("width", wastedPct + "%").set("background", "var(--fw-chart-wasted)");
        Div split = new Div(consumed, wasted);
        split.addClassName("fw-split");
        split.getElement().setAttribute("role", "img");
        split.getElement().setAttribute("aria-label",
                s.consumedTotal() + " eaten and " + s.wastedTotal() + " wasted");
        Div legend = new Div(
                legendItem("var(--fw-chart-consumed)", "Eaten: " + s.consumedTotal() + " (" + (total == 0 ? 0 : 100 - wastedPct) + "%)"),
                legendItem("var(--fw-chart-wasted)", "Wasted: " + s.wastedTotal() + " (" + wastedPct + "%)"));
        legend.addClassName("fw-legend");
        return chartCard("Eaten vs wasted", "All finished items", new Div(split, legend));
    }

    private static Span legendItem(String color, String text) {
        Span swatch = new Span();
        swatch.addClassName("fw-swatch");
        swatch.getStyle().set("background", color);
        return new Span(swatch, new Span(text));
    }

    private static Component chartCard(String title, String subtitle, Component chart) {
        H2 h = new H2(title);
        h.addClassName("fw-chart-title");
        Paragraph p = new Paragraph(subtitle);
        p.addClassName("fw-chart-sub");
        return Ui.card(h, p, chart);
    }
}

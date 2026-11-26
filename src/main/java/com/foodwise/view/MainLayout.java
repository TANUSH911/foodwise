package com.foodwise.view;

import com.foodwise.entity.FoodItem;
import com.foodwise.entity.Notification;
import com.foodwise.entity.Urgency;
import com.foodwise.service.FoodItemService;
import com.foodwise.service.NotificationService;
import com.foodwise.view.components.ItemFormDialog;
import com.foodwise.view.components.Ui;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * App shell: brand, navigation (top pill bar on desktop, dark bottom bar on phones),
 * the "+" add-item button and the notification bell.
 */
public class MainLayout extends Div implements RouterLayout, AfterNavigationObserver {

    private static final DateTimeFormatter ALERT_TIME = DateTimeFormatter.ofPattern("d MMM, h:mm a");

    private final FoodItemService foodItems;
    private final NotificationService notifications;
    private final Main content = new Main();
    private final NativeButton bell = new NativeButton();
    private final Span bellBadge = new Span();
    private HasElement currentView;

    public MainLayout(FoodItemService foodItems, NotificationService notifications) {
        this.foodItems = foodItems;
        this.notifications = notifications;
        addClassName("fw-shell");

        RouterLink brand = new RouterLink("", DashboardView.class);
        Span mark = new Span(Ui.icon("leaf"));
        mark.addClassName("fw-brand-mark");
        brand.add(mark, new Span("FoodWise"));
        brand.addClassName("fw-brand");
        brand.getElement().setAttribute("aria-label", "FoodWise home");

        NativeButton add = new NativeButton();
        add.add(Ui.icon("plus", "l"));
        add.addClassName("fw-nav-add");
        add.getElement().setAttribute("aria-label", "Add food item");
        add.getElement().setAttribute("title", "Add food item");
        add.addClickListener(e -> openAddItem());

        Nav nav = new Nav(
                link("Home", "house", DashboardView.class),
                link("Pantry", "refrigerator", PantryView.class),
                add,
                link("Recipes", "chef-hat", RecipesView.class),
                link("Stats", "chart-column", StatsView.class));
        nav.addClassName("fw-nav");
        nav.getElement().setAttribute("aria-label", "Main");

        bell.add(Ui.icon("bell"), bellBadge);
        bell.addClassName("fw-icon-btn");
        bellBadge.addClassName("fw-bell-badge");
        bell.addClickListener(e -> openAlerts());
        Div actions = new Div(bell);
        actions.addClassName("fw-header-actions");

        Header header = new Header(brand, nav, actions);
        header.addClassName("fw-header");
        content.addClassName("fw-content");
        add(header, content);

        long unread = notifications.unreadCount();
        if (unread > 0) {
            Ui.warningToast("You have " + unread + " expiry alert" + (unread == 1 ? "" : "s") + ". Tap the bell to see them.");
        }
    }

    @Override
    public void showRouterLayoutContent(HasElement view) {
        currentView = view;
        content.getElement().removeAllChildren();
        content.getElement().appendChild(view.getElement());
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        updateBell();
    }

    private RouterLink link(String label, String iconName, Class<? extends Component> view) {
        Span text = new Span(label);
        text.addClassName("fw-nav-label");
        RouterLink link = new RouterLink("", view);
        link.add(Ui.icon(iconName), text);
        link.setHighlightCondition(HighlightConditions.sameLocation());
        link.getElement().setAttribute("aria-label", label);
        link.getElement().setAttribute("title", label);
        return link;
    }

    private void openAddItem() {
        new ItemFormDialog(new FoodItem(), item -> {
            FoodItem saved = foodItems.save(item);
            long days = saved.daysLeft(LocalDate.now());
            if (days <= NotificationService.ALERT_DAYS) {
                Ui.warningToast("Added " + saved.getName() + ". Heads up: it " + Urgency.describe(days).toLowerCase() + "!");
            } else {
                Ui.toast("Added " + saved.getName() + " to your pantry");
            }
            refreshCurrentView();
        }).open();
    }

    private void openAlerts() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Expiry alerts");
        dialog.setWidth("min(440px, 96vw)");
        List<Notification> latest = notifications.latest();
        if (latest.isEmpty()) {
            dialog.add(new Span("No alerts yet. We'll let you know when something is about to expire."));
        }
        for (Notification n : latest) {
            Span text = new Span(n.getMessage());
            text.addClassName("fw-alert-text");
            Span time = new Span(n.getCreatedAt().format(ALERT_TIME));
            time.addClassName("fw-alert-time");
            Div body = new Div(text, new Div(time));
            Div row = new Div(Ui.icon("clock"), body);
            row.addClassName("fw-alert");
            if (!n.isRead()) row.addClassName("unread");
            dialog.add(row);
        }
        Button markRead = new Button("Mark all as read", e -> {
            notifications.markAllRead();
            updateBell();
            dialog.close();
        });
        markRead.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        markRead.setEnabled(notifications.unreadCount() > 0);
        Button close = new Button("Close", e -> dialog.close());
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(close, markRead);
        dialog.open();
    }

    private void updateBell() {
        long unread = notifications.unreadCount();
        bellBadge.setText(unread > 0 ? String.valueOf(unread) : "");
        bell.getElement().setAttribute("aria-label", "Expiry alerts, " + unread + " unread");
        bell.getElement().setAttribute("title", "Expiry alerts");
    }

    /** Reload the visible screen after the layout changed data (e.g. added an item). */
    void refreshCurrentView() {
        if (currentView instanceof Refreshable refreshable) {
            refreshable.refresh();
        }
        updateBell();
    }
}

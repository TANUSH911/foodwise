package com.foodwise.view.components;

import com.foodwise.entity.FoodCategory;
import com.foodwise.entity.FoodItem;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Add / edit form. Validation rules come from the annotations on {@link FoodItem}
 * plus the "expiry not before purchase" rule below (also enforced by a SQL CHECK).
 */
public class ItemFormDialog extends Dialog {

    private static final Locale INDIA = Locale.forLanguageTag("en-IN");

    private final TextField name = new TextField("Name");
    private final ComboBox<FoodCategory> category = new ComboBox<>("Category");
    private final BigDecimalField quantity = new BigDecimalField("Quantity");
    private final ComboBox<String> unit = new ComboBox<>("Unit");
    private final BigDecimalField price = new BigDecimalField("Price (optional)");
    private final DatePicker purchaseDate = new DatePicker("Purchase date");
    private final DatePicker expiryDate = new DatePicker("Expiry date");
    private final BeanValidationBinder<FoodItem> binder = new BeanValidationBinder<>(FoodItem.class);
    private boolean expiryTouched;

    public ItemFormDialog(FoodItem item, Consumer<FoodItem> onSave) {
        boolean isNew = item.getId() == null;
        expiryTouched = !isNew;
        setHeaderTitle(isNew ? "Add food item" : "Edit " + item.getName());
        setWidth("min(560px, 96vw)");

        name.setPlaceholder("e.g. Milk");
        name.setAutofocus(true);
        category.setItems(FoodCategory.values());
        category.setItemLabelGenerator(FoodCategory::getLabel);
        unit.setItems("pcs", "g", "kg", "ml", "L", "pack", "dozen");
        price.setPrefixComponent(new Span("₹"));
        purchaseDate.setLocale(INDIA);
        expiryDate.setLocale(INDIA);
        expiryDate.setHelperText("Filled in from the category; change it if the pack says otherwise");

        // Shelf-life defaults: picking a category pre-fills the expiry date until the user edits it.
        category.addValueChangeListener(e -> suggestExpiry());
        purchaseDate.addValueChangeListener(e -> suggestExpiry());
        expiryDate.addValueChangeListener(e -> {
            if (e.isFromClient()) expiryTouched = true;
        });

        binder.forField(name).bind("name");
        binder.forField(category).bind("category");
        binder.forField(quantity).bind("quantity");
        binder.forField(unit).bind("unit");
        binder.forField(price).bind("price");
        binder.forField(purchaseDate).bind("purchaseDate");
        binder.forField(expiryDate)
                .withValidator(d -> d == null || purchaseDate.getValue() == null || !d.isBefore(purchaseDate.getValue()),
                        "Expiry date can't be before the purchase date")
                .bind("expiryDate");
        binder.readBean(item);
        if (isNew) {
            quantity.setValue(BigDecimal.ONE);
            unit.setValue("pcs");
            purchaseDate.setValue(LocalDate.now());
        }

        FormLayout form = new FormLayout(name, category, quantity, unit, price, purchaseDate, expiryDate);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("420px", 2));
        form.setColspan(name, 2);
        form.setColspan(expiryDate, 2);
        add(form);

        Button cancel = new Button("Cancel", e -> close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Button save = new Button(isNew ? "Add to pantry" : "Save changes", e -> {
            if (binder.writeBeanIfValid(item)) {
                onSave.accept(item);
                close();
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(cancel, save);
    }

    private void suggestExpiry() {
        if (!expiryTouched && category.getValue() != null && purchaseDate.getValue() != null) {
            expiryDate.setValue(purchaseDate.getValue().plusDays(category.getValue().getDefaultShelfDays()));
        }
    }
}

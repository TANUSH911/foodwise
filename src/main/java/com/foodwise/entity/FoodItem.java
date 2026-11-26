package com.foodwise.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A food item in the pantry. Maps to the SQL table {@code food_item}. */
@Entity
@Table(name = "food_item")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Give the item a name")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Pick a category")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FoodCategory category;

    @NotNull(message = "Enter a quantity")
    @DecimalMin(value = "0.01", message = "Quantity must be more than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @NotBlank(message = "Pick a unit")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String unit;

    @PositiveOrZero(message = "Price can't be negative")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Enter the purchase date")
    @Column(nullable = false)
    private LocalDate purchaseDate;

    @NotNull(message = "Enter the expiry date")
    @Column(nullable = false)
    private LocalDate expiryDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemStatus status = ItemStatus.ACTIVE;

    /** Day the item was consumed or wasted; null while ACTIVE. */
    private LocalDate statusDate;

    public FoodItem() {
    }

    public FoodItem(String name, FoodCategory category, BigDecimal quantity, String unit,
                    BigDecimal price, LocalDate purchaseDate, LocalDate expiryDate) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.price = price;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
    }

    public long daysLeft(LocalDate today) {
        return Urgency.daysLeft(expiryDate, today);
    }

    public Urgency urgency(LocalDate today) {
        return Urgency.of(expiryDate, today);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public FoodCategory getCategory() { return category; }
    public void setCategory(FoodCategory category) { this.category = category; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }
    public LocalDate getStatusDate() { return statusDate; }
    public void setStatusDate(LocalDate statusDate) { this.statusDate = statusDate; }

    @Override
    public String toString() {
        return "FoodItem{id=" + id + ", name='" + name + "', expiry=" + expiryDate + ", status=" + status + '}';
    }
}

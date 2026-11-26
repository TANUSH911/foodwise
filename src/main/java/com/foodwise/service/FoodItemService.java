package com.foodwise.service;

import com.foodwise.entity.FoodItem;
import com.foodwise.entity.ItemStatus;
import com.foodwise.repository.FoodItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/** Pantry management: add, edit, delete, mark consumed / wasted. */
@Service
@Transactional
public class FoodItemService {

    private final FoodItemRepository repository;
    private final NotificationService notifications;

    public FoodItemService(FoodItemRepository repository, NotificationService notifications) {
        this.repository = repository;
        this.notifications = notifications;
    }

    /** Items still in the pantry, soonest expiry first. */
    @Transactional(readOnly = true)
    public List<FoodItem> findActive() {
        return repository.findByStatusOrderByExpiryDateAsc(ItemStatus.ACTIVE);
    }

    /** Insert or update. Creates an expiry alert right away if the item is close to expiry. */
    public FoodItem save(FoodItem item) {
        FoodItem saved = repository.save(item);
        notifications.alertIfExpiringSoon(saved);
        return saved;
    }

    /** Hard delete (for mistakes). Use markConsumed / markWasted for real outcomes. */
    public void delete(FoodItem item) {
        repository.deleteById(item.getId());
    }

    public void markConsumed(FoodItem item) {
        changeStatus(item, ItemStatus.CONSUMED);
    }

    public void markConsumed(Collection<FoodItem> items) {
        items.forEach(this::markConsumed);
    }

    public void markWasted(FoodItem item) {
        changeStatus(item, ItemStatus.WASTED);
    }

    /** Undo for markConsumed / markWasted / cooking: puts the items back in the pantry. */
    public void restore(Collection<FoodItem> items) {
        items.forEach(item -> repository.findById(item.getId()).ifPresent(current -> {
            current.setStatus(ItemStatus.ACTIVE);
            current.setStatusDate(null);
        }));
    }

    private void changeStatus(FoodItem item, ItemStatus status) {
        FoodItem current = repository.findById(item.getId())
                .orElseThrow(() -> new IllegalArgumentException("Item no longer exists: " + item.getId()));
        current.setStatus(status);
        current.setStatusDate(LocalDate.now());
    }
}

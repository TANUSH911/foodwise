package com.foodwise.service;

import com.foodwise.entity.FoodItem;
import com.foodwise.entity.ItemStatus;
import com.foodwise.entity.Notification;
import com.foodwise.entity.Urgency;
import com.foodwise.repository.FoodItemRepository;
import com.foodwise.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** In-app expiry alerts. At most one alert per food item. */
@Service
@Transactional
public class NotificationService {

    /** Alert when an item expires within this many days. */
    public static final int ALERT_DAYS = 2;

    private final NotificationRepository notifications;
    private final FoodItemRepository foodItems;

    public NotificationService(NotificationRepository notifications, FoodItemRepository foodItems) {
        this.notifications = notifications;
        this.foodItems = foodItems;
    }

    /** Called by the daily scheduler. Returns how many new alerts were created. */
    public int createExpiryAlerts() {
        LocalDate limit = LocalDate.now().plusDays(ALERT_DAYS);
        int created = 0;
        for (FoodItem item : foodItems.findByStatusAndExpiryDateLessThanEqual(ItemStatus.ACTIVE, limit)) {
            if (alertIfExpiringSoon(item)) created++;
        }
        return created;
    }

    /** Creates an alert for this item if it is close to expiry and has none yet. */
    public boolean alertIfExpiringSoon(FoodItem item) {
        long days = item.daysLeft(LocalDate.now());
        if (item.getStatus() != ItemStatus.ACTIVE || days > ALERT_DAYS
                || notifications.existsByFoodItemId(item.getId())) {
            return false;
        }
        notifications.save(new Notification(item, item.getName() + ": " + Urgency.describe(days).toLowerCase()));
        return true;
    }

    @Transactional(readOnly = true)
    public List<Notification> latest() {
        return notifications.findTop20ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return notifications.countByReadFalse();
    }

    public void markAllRead() {
        notifications.markAllRead();
    }
}

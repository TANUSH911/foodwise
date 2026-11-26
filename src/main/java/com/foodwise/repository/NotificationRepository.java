package com.foodwise.repository;

import com.foodwise.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** SQL access for {@code notification}. */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop20ByOrderByCreatedAtDesc();

    long countByReadFalse();

    boolean existsByFoodItemId(Long foodItemId);

    @Modifying
    @Query("update Notification n set n.read = true where n.read = false")
    int markAllRead();
}

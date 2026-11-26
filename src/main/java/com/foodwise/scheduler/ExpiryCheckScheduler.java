package com.foodwise.scheduler;

import com.foodwise.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Daily job that turns "about to expire" items into in-app alerts. */
@Component
public class ExpiryCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiryCheckScheduler.class);

    private final NotificationService notifications;

    public ExpiryCheckScheduler(NotificationService notifications) {
        this.notifications = notifications;
    }

    @Scheduled(cron = "${foodwise.alerts.cron:0 0 8 * * *}")
    public void dailyCheck() {
        int created = notifications.createExpiryAlerts();
        log.info("Expiry check done: {} new alert(s)", created);
    }

    /** Also run once at startup so alerts exist even if the app was off at 8am. */
    @EventListener(ApplicationReadyEvent.class)
    public void checkOnStartup() {
        dailyCheck();
    }
}

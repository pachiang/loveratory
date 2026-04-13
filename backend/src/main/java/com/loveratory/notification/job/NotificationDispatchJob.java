package com.loveratory.notification.job;

import com.loveratory.notification.service.NotificationDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationDispatchJob {

    private final NotificationDispatchService notificationDispatchService;

    @Scheduled(cron = "0 * * * * *")
    @SchedulerLock(name = "dispatchDueNotifications", lockAtLeastFor = "30s", lockAtMostFor = "5m")
    public void dispatchDueNotifications() {
        int processedCount = notificationDispatchService.dispatchDueNotifications();
        if (processedCount > 0) {
            log.info("Processed pending notifications. count: {}", processedCount);
        }
    }
}

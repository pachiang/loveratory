package com.loveratory.notification.service;

import com.loveratory.experiment.dto.internal.NotificationConfig;
import com.loveratory.experiment.entity.ExperimentEntity;
import com.loveratory.notification.entity.NotificationChannel;
import com.loveratory.notification.entity.NotificationEntity;
import com.loveratory.notification.entity.NotificationStatus;
import com.loveratory.notification.entity.NotificationType;
import com.loveratory.notification.manager.NotificationManager;
import com.loveratory.registration.entity.RegistrationEntity;
import com.loveratory.slot.entity.TimeSlotEntity;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RegistrationNotificationService {

    private final NotificationManager notificationManager;

    @Transactional(rollbackFor = Exception.class)
    public void scheduleNotifications(@NonNull RegistrationEntity registration,
                                      @NonNull TimeSlotEntity slot,
                                      @NonNull ExperimentEntity experiment) {
        NotificationConfig config = experiment.getNotificationConfig();
        if (config == null || !config.isEnabled()) {
            return;
        }

        ZonedDateTime now = ZonedDateTime.now();
        List<NotificationEntity> notifications = new ArrayList<>();

        if (config.isOnRegistration()) {
            notifications.add(createNotification(
                    registration.getId(),
                    NotificationType.REGISTRATION_CONFIRMED,
                    now));
        }

        if (slot.getStartTime().isAfter(now) && config.getReminders() != null) {
            config.getReminders().stream()
                    .filter(reminderDays -> reminderDays != null && reminderDays > 0)
                    .distinct()
                    .sorted(Comparator.reverseOrder())
                    .map(reminderDays -> slot.getStartTime().minusDays(reminderDays))
                    .map(reminderTime -> reminderTime.isAfter(now) ? reminderTime : now)
                    .map(reminderTime -> createNotification(
                            registration.getId(),
                            NotificationType.REMINDER,
                            reminderTime))
                    .forEach(notifications::add);
        }

        if (!notifications.isEmpty()) {
            notificationManager.saveAll(notifications);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void disablePendingNotifications(@NonNull java.util.UUID registrationId, @NonNull String reason) {
        List<NotificationEntity> notifications = notificationManager.findPendingByRegistrationId(registrationId);
        if (notifications.isEmpty()) {
            return;
        }

        notifications.forEach(notification -> {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(reason);
        });
        notificationManager.saveAll(notifications);
    }

    private NotificationEntity createNotification(java.util.UUID registrationId,
                                                  NotificationType type,
                                                  ZonedDateTime scheduledAt) {
        NotificationEntity notification = new NotificationEntity();
        notification.setRegistrationId(registrationId);
        notification.setType(type);
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setScheduledAt(scheduledAt);
        notification.setRetryCount(0);
        return notification;
    }
}

package com.loveratory.notification.manager;

import com.loveratory.notification.entity.NotificationEntity;
import com.loveratory.notification.entity.NotificationStatus;
import com.loveratory.notification.repository.NotificationRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class NotificationManager {

    private final NotificationRepository notificationRepository;

    public List<NotificationEntity> findDuePendingNotifications(@NonNull ZonedDateTime now) {
        return notificationRepository.findTop100ByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
                NotificationStatus.PENDING,
                now);
    }

    public List<NotificationEntity> findPendingByRegistrationId(@NonNull UUID registrationId) {
        return notificationRepository.findByRegistrationIdAndStatus(registrationId, NotificationStatus.PENDING);
    }

    public NotificationEntity save(@NonNull NotificationEntity notificationEntity) {
        return notificationRepository.save(notificationEntity);
    }

    public List<NotificationEntity> saveAll(@NonNull List<NotificationEntity> notificationEntities) {
        return notificationRepository.saveAll(notificationEntities);
    }
}

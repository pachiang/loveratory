package com.loveratory.notification.repository;

import com.loveratory.notification.entity.NotificationEntity;
import com.loveratory.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID>,
        JpaSpecificationExecutor<NotificationEntity> {

    List<NotificationEntity> findTop100ByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
            NotificationStatus status,
            ZonedDateTime scheduledAt);

    List<NotificationEntity> findByRegistrationIdAndStatus(UUID registrationId, NotificationStatus status);
}

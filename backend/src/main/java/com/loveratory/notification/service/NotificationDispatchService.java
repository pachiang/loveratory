package com.loveratory.notification.service;

import com.loveratory.experiment.dto.internal.NotificationConfig;
import com.loveratory.experiment.entity.ExperimentEntity;
import com.loveratory.experiment.manager.ExperimentManager;
import com.loveratory.notification.entity.NotificationEntity;
import com.loveratory.notification.entity.NotificationStatus;
import com.loveratory.notification.entity.NotificationType;
import com.loveratory.notification.manager.NotificationManager;
import com.loveratory.registration.entity.RegistrationEntity;
import com.loveratory.registration.entity.RegistrationStatus;
import com.loveratory.registration.manager.RegistrationManager;
import com.loveratory.slot.entity.TimeSlotEntity;
import com.loveratory.slot.manager.TimeSlotManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationDispatchService {

    private static final int MAX_RETRY_COUNT = 3;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z");

    private final NotificationManager notificationManager;
    private final RegistrationManager registrationManager;
    private final TimeSlotManager timeSlotManager;
    private final ExperimentManager experimentManager;
    private final EmailSenderService emailSenderService;

    @Value("${app.frontend-base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    @Transactional(rollbackFor = Exception.class)
    public int dispatchDueNotifications() {
        List<NotificationEntity> notifications = notificationManager.findDuePendingNotifications(ZonedDateTime.now());
        notifications.forEach(this::dispatchNotificationSafely);
        return notifications.size();
    }

    private void dispatchNotificationSafely(NotificationEntity notification) {
        try {
            RegistrationEntity registration = registrationManager.findByIdOrThrow(notification.getRegistrationId());
            TimeSlotEntity slot = timeSlotManager.findByIdOrThrow(registration.getTimeSlotId());
            ExperimentEntity experiment = experimentManager.findByIdOrThrow(slot.getExperimentId());

            if (registration.getStatus() != RegistrationStatus.CONFIRMED) {
                markAsFailed(notification, "Registration is no longer confirmed");
                return;
            }

            NotificationConfig config = experiment.getNotificationConfig();
            if (config == null || !config.isEnabled()) {
                markAsFailed(notification, "Experiment notifications are disabled");
                return;
            }

            if (notification.getType() == NotificationType.REMINDER
                    && !slot.getStartTime().isAfter(ZonedDateTime.now())) {
                markAsFailed(notification, "Time slot has already started");
                return;
            }

            emailSenderService.sendPlainText(
                    registration.getParticipantEmail(),
                    buildSubject(notification, experiment),
                    buildBody(notification, registration, slot, experiment));

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(ZonedDateTime.now());
            notification.setErrorMessage(null);
            notificationManager.save(notification);
        } catch (Exception exception) {
            log.warn("Failed to dispatch notification. notificationId: {}, reason: {}",
                    notification.getId(), exception.getMessage());
            retryOrFail(notification, exception.getMessage());
        }
    }

    private String buildSubject(NotificationEntity notification, ExperimentEntity experiment) {
        return switch (notification.getType()) {
            case REGISTRATION_CONFIRMED -> "[Loveratory] Registration confirmed: " + experiment.getName();
            case REMINDER -> "[Loveratory] Reminder: " + experiment.getName();
        };
    }

    private String buildBody(NotificationEntity notification,
                             RegistrationEntity registration,
                             TimeSlotEntity slot,
                             ExperimentEntity experiment) {
        String participantName = registration.getParticipantName() != null
                ? registration.getParticipantName()
                : registration.getParticipantEmail();
        String slotTime = DATE_TIME_FORMATTER.format(slot.getStartTime())
                + " - "
                + DATE_TIME_FORMATTER.format(slot.getEndTime());
        String cancelLink = frontendBaseUrl + "/cancel/" + registration.getCancelToken();

        if (notification.getType() == NotificationType.REGISTRATION_CONFIRMED) {
            StringBuilder builder = new StringBuilder()
                    .append("Hello ").append(participantName).append(",\n\n")
                    .append("Your registration is confirmed.\n\n")
                    .append("Experiment: ").append(experiment.getName()).append("\n")
                    .append("Time: ").append(slotTime).append("\n");

            if (experiment.getLocation() != null) {
                builder.append("Location: ").append(experiment.getLocation()).append("\n");
            }

            if (Boolean.TRUE.equals(experiment.getAllowParticipantCancel())) {
                builder.append("\nCancel link: ").append(cancelLink).append("\n");
            }

            return builder.toString();
        }

        StringBuilder builder = new StringBuilder()
                .append("Hello ").append(participantName).append(",\n\n")
                .append("This is a reminder for your upcoming experiment.\n\n")
                .append("Experiment: ").append(experiment.getName()).append("\n")
                .append("Time: ").append(slotTime).append("\n");

        if (experiment.getLocation() != null) {
            builder.append("Location: ").append(experiment.getLocation()).append("\n");
        }

        if (Boolean.TRUE.equals(experiment.getAllowParticipantCancel())) {
            builder.append("\nCancel link: ").append(cancelLink).append("\n");
        }

        return builder.toString();
    }

    private void retryOrFail(NotificationEntity notification, String errorMessage) {
        int retryCount = notification.getRetryCount() == null ? 0 : notification.getRetryCount();
        retryCount++;

        notification.setRetryCount(retryCount);
        notification.setErrorMessage(truncate(errorMessage));

        if (retryCount >= MAX_RETRY_COUNT) {
            notification.setStatus(NotificationStatus.FAILED);
        } else {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setScheduledAt(ZonedDateTime.now().plusMinutes(5L * retryCount));
        }

        notificationManager.save(notification);
    }

    private void markAsFailed(NotificationEntity notification, String errorMessage) {
        notification.setStatus(NotificationStatus.FAILED);
        notification.setErrorMessage(truncate(errorMessage));
        notificationManager.save(notification);
    }

    private String truncate(String message) {
        if (message == null || message.length() <= 1000) {
            return message;
        }
        return message.substring(0, 1000);
    }
}

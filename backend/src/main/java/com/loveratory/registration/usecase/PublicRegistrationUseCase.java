package com.loveratory.registration.usecase;

import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.experiment.dto.internal.FormConfig;
import com.loveratory.experiment.entity.ExperimentEntity;
import com.loveratory.experiment.entity.ExperimentStatus;
import com.loveratory.experiment.manager.ExperimentManager;
import com.loveratory.registration.dto.request.ParticipantRegistrationRequest;
import com.loveratory.registration.dto.response.PublicExperimentResponse;
import com.loveratory.registration.dto.response.PublicRegistrationResponse;
import com.loveratory.registration.dto.response.PublicTimeSlotResponse;
import com.loveratory.registration.entity.RegistrationEntity;
import com.loveratory.registration.entity.RegistrationStatus;
import com.loveratory.registration.manager.RegistrationManager;
import com.loveratory.notification.service.RegistrationNotificationService;
import com.loveratory.slot.entity.TimeSlotEntity;
import com.loveratory.slot.entity.TimeSlotStatus;
import com.loveratory.slot.manager.TimeSlotManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 受試者報名業務邏輯（公開）。
 * 處理受試者的實驗查詢、報名、查看報名與取消報名等操作，不需要登入。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PublicRegistrationUseCase {

    private final RegistrationManager registrationManager;
    private final TimeSlotManager timeSlotManager;
    private final ExperimentManager experimentManager;
    private final RegistrationNotificationService registrationNotificationService;

    /**
     * 根據 slug 查詢公開實驗資訊。
     * 僅限狀態為 OPEN 的實驗可查詢。
     *
     * @param slug 實驗連結代碼
     * @return 公開實驗回應
     */
    @Transactional(readOnly = true)
    public PublicExperimentResponse findExperimentBySlug(@NonNull String slug) {
        ExperimentEntity experiment = experimentManager.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPERIMENT_NOT_FOUND));

        if (experiment.getStatus() != ExperimentStatus.OPEN) {
            throw new BusinessException(ErrorCode.INVALID_EXPERIMENT_STATUS);
        }

        ZonedDateTime now = ZonedDateTime.now();
        List<TimeSlotEntity> availableSlots = timeSlotManager.findAvailableByExperimentId(
                experiment.getId()).stream()
                .filter(slot -> slot.getStartTime().isAfter(now))
                .toList();

        List<PublicTimeSlotResponse> slotResponses = availableSlots.stream()
                .map(PublicTimeSlotResponse::fromEntity)
                .toList();

        return PublicExperimentResponse.builder()
                .name(experiment.getName())
                .description(experiment.getDescription())
                .location(experiment.getLocation())
                .durationMinutes(experiment.getDurationMinutes())
                .formConfig(experiment.getFormConfig())
                .availableSlots(slotResponses)
                .build();
    }

    /**
     * 受試者報名。
     * 驗證實驗狀態、時段狀態、重複報名、表單必填欄位與名額後建立報名記錄。
     *
     * @param slug    實驗連結代碼
     * @param request 報名請求
     * @return 公開報名回應
     */
    @Transactional(rollbackFor = Exception.class)
    public PublicRegistrationResponse register(@NonNull String slug,
                                                @NonNull ParticipantRegistrationRequest request) {
        ExperimentEntity experiment = experimentManager.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPERIMENT_NOT_FOUND));

        if (experiment.getStatus() != ExperimentStatus.OPEN) {
            throw new BusinessException(ErrorCode.INVALID_EXPERIMENT_STATUS);
        }

        TimeSlotEntity slot = timeSlotManager.findByIdOrThrow(request.getSlotId());

        if (!slot.getExperimentId().equals(experiment.getId())) {
            throw new BusinessException(ErrorCode.SLOT_NOT_FOUND);
        }

        if (slot.getStatus() != TimeSlotStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.INVALID_SLOT_STATUS);
        }

        if (!slot.getStartTime().isAfter(ZonedDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_SLOT_STATUS, "只能報名尚未開始的時段");
        }

        // 檢查重複報名
        if (!Boolean.TRUE.equals(experiment.getAllowDuplicateEmail())) {
            List<TimeSlotEntity> allSlots = timeSlotManager.findByExperimentId(experiment.getId());
            List<UUID> allSlotIds = allSlots.stream()
                    .map(TimeSlotEntity::getId)
                    .toList();

            if (registrationManager.existsConfirmedRegistration(allSlotIds, request.getEmail())) {
                throw new BusinessException(ErrorCode.DUPLICATE_REGISTRATION);
            }
        }

        // 驗證表單必填欄位
        validateFormFields(experiment.getFormConfig(), request);

        // 檢查名額
        if (slot.getCurrentCount() >= slot.getCapacity()) {
            throw new BusinessException(ErrorCode.SLOT_FULL);
        }

        // 建立報名
        RegistrationEntity registration = new RegistrationEntity();
        registration.setTimeSlotId(slot.getId());
        registration.setParticipantEmail(request.getEmail());
        registration.setParticipantName(request.getName());
        registration.setParticipantPhone(request.getPhone());
        registration.setParticipantStudentId(request.getStudentId());
        registration.setParticipantAge(request.getAge());
        registration.setParticipantGender(request.getGender());
        registration.setParticipantDominantHand(request.getDominantHand());
        registration.setParticipantNotes(request.getNotes());
        registration.setCancelToken(UUID.randomUUID().toString());
        registration.setStatus(RegistrationStatus.CONFIRMED);
        registration.setRegisteredAt(ZonedDateTime.now());

        RegistrationEntity savedRegistration = registrationManager.save(registration);

        // 更新時段報名人數
        slot.setCurrentCount(slot.getCurrentCount() + 1);
        if (slot.getCurrentCount() >= slot.getCapacity()) {
            slot.setStatus(TimeSlotStatus.FULL);
        }
        timeSlotManager.save(slot);
        registrationNotificationService.scheduleNotifications(savedRegistration, slot, experiment);

        return PublicRegistrationResponse.of(savedRegistration, slot, experiment);
    }

    /**
     * 根據取消 token 查詢報名資訊。
     *
     * @param cancelToken 取消 token
     * @return 公開報名回應
     */
    @Transactional(readOnly = true)
    public PublicRegistrationResponse findRegistrationByToken(@NonNull String cancelToken) {
        RegistrationEntity registration = registrationManager.findByCancelTokenOrThrow(cancelToken);
        TimeSlotEntity slot = timeSlotManager.findByIdOrThrow(registration.getTimeSlotId());
        ExperimentEntity experiment = experimentManager.findByIdOrThrow(slot.getExperimentId());

        return PublicRegistrationResponse.of(registration, slot, experiment);
    }

    /**
     * 受試者取消報名。
     * 僅限實驗允許取消且報名狀態為 CONFIRMED 時可取消。
     *
     * @param cancelToken 取消 token
     * @return 公開報名回應
     */
    @Transactional(rollbackFor = Exception.class)
    public PublicRegistrationResponse cancelRegistration(@NonNull String cancelToken) {
        RegistrationEntity registration = registrationManager.findByCancelTokenOrThrow(cancelToken);

        if (registration.getStatus() != RegistrationStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.REGISTRATION_ALREADY_CANCELLED);
        }

        TimeSlotEntity slot = timeSlotManager.findByIdOrThrow(registration.getTimeSlotId());
        ExperimentEntity experiment = experimentManager.findByIdOrThrow(slot.getExperimentId());

        if (!Boolean.TRUE.equals(experiment.getAllowParticipantCancel())) {
            throw new BusinessException(ErrorCode.REGISTRATION_CANCEL_NOT_ALLOWED);
        }

        // 取消報名
        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setCancelledAt(ZonedDateTime.now());
        RegistrationEntity savedRegistration = registrationManager.save(registration);

        // 更新時段報名人數
        slot.setCurrentCount(Math.max(0, slot.getCurrentCount() - 1));
        if (slot.getStatus() == TimeSlotStatus.FULL && slot.getCurrentCount() < slot.getCapacity()) {
            slot.setStatus(TimeSlotStatus.AVAILABLE);
        }
        timeSlotManager.save(slot);
        registrationNotificationService.disablePendingNotifications(
                savedRegistration.getId(),
                "Registration was cancelled by participant");

        return PublicRegistrationResponse.of(savedRegistration, slot, experiment);
    }

    /**
     * 驗證表單必填欄位。
     * 根據表單設定檢查受試者是否填寫了所有可見且必填的欄位。
     *
     * @param formConfig 表單設定
     * @param request    報名請求
     */
    private void validateFormFields(FormConfig formConfig, ParticipantRegistrationRequest request) {
        if (formConfig == null || formConfig.getFields() == null) {
            return;
        }

        for (FormConfig.FormField field : formConfig.getFields()) {
            if (!field.isVisible() || !field.isRequired()) {
                continue;
            }

            String value = getFieldValue(field.getKey(), request);
            if (value == null || value.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER,
                        String.format("欄位「%s」為必填", field.getLabel()));
            }
        }
    }

    /**
     * 根據欄位代碼從報名請求中取得對應值。
     *
     * @param key     欄位代碼
     * @param request 報名請求
     * @return 欄位值的字串表示
     */
    private String getFieldValue(String key, ParticipantRegistrationRequest request) {
        return switch (key) {
            case "email" -> request.getEmail();
            case "name" -> request.getName();
            case "phone" -> request.getPhone();
            case "student_id" -> request.getStudentId();
            case "age" -> request.getAge() != null ? request.getAge().toString() : null;
            case "gender" -> request.getGender();
            case "dominant_hand" -> request.getDominantHand();
            case "notes" -> request.getNotes();
            default -> null;
        };
    }
}

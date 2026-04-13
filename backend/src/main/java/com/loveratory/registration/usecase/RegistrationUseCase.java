package com.loveratory.registration.usecase;

import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.common.util.SecurityUtil;
import com.loveratory.experiment.entity.ExperimentEntity;
import com.loveratory.experiment.manager.ExperimentManager;
import com.loveratory.lab.entity.LabMemberRole;
import com.loveratory.lab.entity.LabMemberStatus;
import com.loveratory.lab.manager.LabMemberManager;
import com.loveratory.project.entity.ProjectEntity;
import com.loveratory.project.manager.ProjectInvestigatorManager;
import com.loveratory.project.manager.ProjectManager;
import com.loveratory.registration.dto.request.RegistrationStatusUpdateRequest;
import com.loveratory.registration.dto.response.RegistrationResponse;
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
 * 報名管理業務邏輯（後台）。
 * 處理報名的查詢與狀態更新等管理操作。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RegistrationUseCase {

    private final RegistrationManager registrationManager;
    private final TimeSlotManager timeSlotManager;
    private final ExperimentManager experimentManager;
    private final ProjectManager projectManager;
    private final ProjectInvestigatorManager projectInvestigatorManager;
    private final LabMemberManager labMemberManager;
    private final RegistrationNotificationService registrationNotificationService;

    /**
     * 查詢實驗的所有報名記錄。
     *
     * @param experimentId 實驗 ID
     * @return 報名回應列表
     */
    @Transactional(readOnly = true)
    public List<RegistrationResponse> findRegistrations(@NonNull UUID experimentId) {
        ExperimentEntity experiment = experimentManager.findByIdOrThrow(experimentId);
        verifyExperimentInvestigator(experiment);

        List<TimeSlotEntity> slots = timeSlotManager.findByExperimentId(experimentId);
        List<UUID> slotIds = slots.stream()
                .map(TimeSlotEntity::getId)
                .toList();

        if (slotIds.isEmpty()) {
            return List.of();
        }

        List<RegistrationEntity> registrations = registrationManager.findByTimeSlotIds(slotIds);

        return registrations.stream()
                .map(RegistrationResponse::fromEntity)
                .toList();
    }

    /**
     * 更新報名狀態。
     * 若狀態變更為 CANCELLED，自動設定取消時間。
     *
     * @param registrationId 報名 ID
     * @param request        更新狀態請求
     * @return 報名回應
     */
    @Transactional(rollbackFor = Exception.class)
    public RegistrationResponse updateRegistrationStatus(@NonNull UUID registrationId,
                                                          @NonNull RegistrationStatusUpdateRequest request) {
        RegistrationEntity registration = registrationManager.findByIdOrThrow(registrationId);

        TimeSlotEntity slot = timeSlotManager.findByIdOrThrow(registration.getTimeSlotId());
        ExperimentEntity experiment = experimentManager.findByIdOrThrow(slot.getExperimentId());
        verifyExperimentInvestigator(experiment);

        RegistrationStatus originalStatus = registration.getStatus();
        RegistrationStatus targetStatus = request.getStatus();

        if (originalStatus == RegistrationStatus.CONFIRMED && targetStatus == RegistrationStatus.CANCELLED) {
            slot.setCurrentCount(Math.max(0, slot.getCurrentCount() - 1));
            if (slot.getStatus() == TimeSlotStatus.FULL && slot.getCurrentCount() < slot.getCapacity()) {
                slot.setStatus(TimeSlotStatus.AVAILABLE);
            }
            timeSlotManager.save(slot);
            registration.setCancelledAt(ZonedDateTime.now());
        } else if (originalStatus == RegistrationStatus.CANCELLED && targetStatus == RegistrationStatus.CONFIRMED) {
            if (slot.getCurrentCount() >= slot.getCapacity()) {
                throw new BusinessException(ErrorCode.SLOT_FULL);
            }
            slot.setCurrentCount(slot.getCurrentCount() + 1);
            if (slot.getCurrentCount() >= slot.getCapacity()) {
                slot.setStatus(TimeSlotStatus.FULL);
            } else if (slot.getStatus() != TimeSlotStatus.CANCELLED) {
                slot.setStatus(TimeSlotStatus.AVAILABLE);
            }
            timeSlotManager.save(slot);
            registration.setCancelledAt(null);
        } else if (targetStatus != RegistrationStatus.CANCELLED) {
            registration.setCancelledAt(null);
        }

        registration.setStatus(targetStatus);
        RegistrationEntity savedRegistration = registrationManager.save(registration);

        if (originalStatus == RegistrationStatus.CONFIRMED && targetStatus != RegistrationStatus.CONFIRMED) {
            registrationNotificationService.disablePendingNotifications(
                    savedRegistration.getId(),
                    "Registration status changed to " + targetStatus);
        } else if (originalStatus != RegistrationStatus.CONFIRMED
                && targetStatus == RegistrationStatus.CONFIRMED) {
            registrationNotificationService.scheduleNotifications(savedRegistration, slot, experiment);
        }

        return RegistrationResponse.fromEntity(savedRegistration);
    }

    /**
     * 驗證當前使用者是否為實驗所屬專案的主持人或實驗室管理員。
     * 若兩者皆非，拋出 NOT_PROJECT_INVESTIGATOR 例外。
     *
     * @param experiment 實驗 Entity
     */
    private void verifyExperimentInvestigator(ExperimentEntity experiment) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        ProjectEntity project = projectManager.findByIdOrThrow(experiment.getProjectId());

        boolean isInvestigator = projectInvestigatorManager.existsActiveInvestigator(
                project.getId(), currentUserId);
        if (isInvestigator) {
            return;
        }

        boolean isLabAdmin = labMemberManager.findByLabIdAndUserId(
                        project.getLabId(), currentUserId)
                .filter(member -> member.getStatus() == LabMemberStatus.ACTIVE)
                .filter(member -> member.getRole() == LabMemberRole.LAB_ADMIN)
                .isPresent();
        if (isLabAdmin) {
            return;
        }

        throw new BusinessException(ErrorCode.NOT_PROJECT_INVESTIGATOR);
    }
}

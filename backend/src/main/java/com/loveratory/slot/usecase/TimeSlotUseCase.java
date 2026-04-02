package com.loveratory.slot.usecase;

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
import com.loveratory.slot.dto.request.SlotBatchCreateRequest;
import com.loveratory.slot.dto.request.SlotCreateRequest;
import com.loveratory.slot.dto.request.SlotUpdateRequest;
import com.loveratory.slot.dto.response.TimeSlotResponse;
import com.loveratory.slot.entity.TimeSlotEntity;
import com.loveratory.slot.entity.TimeSlotStatus;
import com.loveratory.slot.manager.TimeSlotManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 時段業務邏輯。
 * 處理時段的建立、查詢、更新與取消等操作。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TimeSlotUseCase {

    private static final ZoneId TAIPEI_ZONE = ZoneId.of("Asia/Taipei");

    private final TimeSlotManager timeSlotManager;
    private final ExperimentManager experimentManager;
    private final ProjectManager projectManager;
    private final ProjectInvestigatorManager projectInvestigatorManager;
    private final LabMemberManager labMemberManager;

    /**
     * 建立時段。
     * 為指定實驗批次建立多個時段，容量未填則使用實驗的預設設定。
     *
     * @param experimentId 實驗 ID
     * @param requests     建立時段請求列表
     * @return 時段回應列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<TimeSlotResponse> createSlots(@NonNull UUID experimentId,
                                               @NonNull List<SlotCreateRequest> requests) {
        ExperimentEntity experiment = experimentManager.findByIdOrThrow(experimentId);
        verifyExperimentInvestigator(experiment);

        List<TimeSlotEntity> slots = requests.stream()
                .map(request -> {
                    TimeSlotEntity slot = new TimeSlotEntity();
                    slot.setExperimentId(experimentId);
                    slot.setStartTime(request.getStartTime());
                    slot.setEndTime(request.getEndTime());
                    slot.setCapacity(request.getCapacity() != null
                            ? request.getCapacity()
                            : experiment.getMaxParticipantsPerSlot());
                    slot.setCurrentCount(0);
                    slot.setStatus(TimeSlotStatus.AVAILABLE);
                    return slot;
                })
                .toList();

        List<TimeSlotEntity> savedSlots = timeSlotManager.saveAll(slots);

        return savedSlots.stream()
                .map(TimeSlotResponse::fromEntity)
                .toList();
    }

    /**
     * 批次建立時段。
     * 根據日期範圍、星期、每日時段區間等參數自動產生多個時段。
     *
     * @param experimentId 實驗 ID
     * @param request      批次建立時段請求
     * @return 時段回應列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<TimeSlotResponse> createSlotsBatch(@NonNull UUID experimentId,
                                                    @NonNull SlotBatchCreateRequest request) {
        ExperimentEntity experiment = experimentManager.findByIdOrThrow(experimentId);
        verifyExperimentInvestigator(experiment);

        Integer capacity = request.getCapacity() != null
                ? request.getCapacity()
                : experiment.getMaxParticipantsPerSlot();
        int breakMinutes = request.getBreakMinutes() != null
                ? request.getBreakMinutes()
                : 0;

        List<TimeSlotEntity> slots = new ArrayList<>();

        for (LocalDate date = request.getStartDate();
             !date.isAfter(request.getEndDate());
             date = date.plusDays(1)) {

            if (!request.getDaysOfWeek().contains(date.getDayOfWeek())) {
                continue;
            }

            LocalTime currentTime = request.getDailyStartTime();

            while (!currentTime.plusMinutes(request.getDurationMinutes())
                    .isAfter(request.getDailyEndTime())) {

                ZonedDateTime startTime = ZonedDateTime.of(date, currentTime, TAIPEI_ZONE);
                ZonedDateTime endTime = ZonedDateTime.of(
                        date, currentTime.plusMinutes(request.getDurationMinutes()), TAIPEI_ZONE);

                TimeSlotEntity slot = new TimeSlotEntity();
                slot.setExperimentId(experimentId);
                slot.setStartTime(startTime);
                slot.setEndTime(endTime);
                slot.setCapacity(capacity);
                slot.setCurrentCount(0);
                slot.setStatus(TimeSlotStatus.AVAILABLE);

                slots.add(slot);

                currentTime = currentTime.plusMinutes(request.getDurationMinutes() + breakMinutes);
            }
        }

        List<TimeSlotEntity> savedSlots = timeSlotManager.saveAll(slots);

        return savedSlots.stream()
                .map(TimeSlotResponse::fromEntity)
                .toList();
    }

    /**
     * 查詢實驗的所有時段。
     *
     * @param experimentId 實驗 ID
     * @return 時段回應列表
     */
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> findSlots(@NonNull UUID experimentId) {
        ExperimentEntity experiment = experimentManager.findByIdOrThrow(experimentId);
        verifyExperimentInvestigator(experiment);

        List<TimeSlotEntity> slots = timeSlotManager.findByExperimentId(experimentId);

        return slots.stream()
                .map(TimeSlotResponse::fromEntity)
                .toList();
    }

    /**
     * 更新時段。
     *
     * @param slotId  時段 ID
     * @param request 更新時段請求
     * @return 時段回應
     */
    @Transactional(rollbackFor = Exception.class)
    public TimeSlotResponse updateSlot(@NonNull UUID slotId,
                                        @NonNull SlotUpdateRequest request) {
        TimeSlotEntity slot = timeSlotManager.findByIdOrThrow(slotId);
        ExperimentEntity experiment = experimentManager.findByIdOrThrow(slot.getExperimentId());
        verifyExperimentInvestigator(experiment);

        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setCapacity(request.getCapacity());

        TimeSlotEntity savedSlot = timeSlotManager.save(slot);
        return TimeSlotResponse.fromEntity(savedSlot);
    }

    /**
     * 取消時段。
     *
     * @param slotId 時段 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelSlot(@NonNull UUID slotId) {
        TimeSlotEntity slot = timeSlotManager.findByIdOrThrow(slotId);
        ExperimentEntity experiment = experimentManager.findByIdOrThrow(slot.getExperimentId());
        verifyExperimentInvestigator(experiment);

        slot.setStatus(TimeSlotStatus.CANCELLED);
        timeSlotManager.save(slot);
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

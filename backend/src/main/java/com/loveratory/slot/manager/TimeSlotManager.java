package com.loveratory.slot.manager;

import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.slot.entity.TimeSlotEntity;
import com.loveratory.slot.entity.TimeSlotStatus;
import com.loveratory.slot.repository.TimeSlotRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 時段資料存取管理器。
 * 封裝 TimeSlotEntity 的所有 CRUD 操作。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TimeSlotManager {

    private final TimeSlotRepository timeSlotRepository;

    /**
     * 根據 ID 查詢時段，找不到時拋出 BusinessException。
     *
     * @param slotId 時段 ID
     * @return 時段 Entity
     */
    public TimeSlotEntity findByIdOrThrow(@NonNull UUID slotId) {
        return timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));
    }

    /**
     * 根據實驗 ID 查詢所有時段，依開始時間升冪排序。
     *
     * @param experimentId 實驗 ID
     * @return 時段列表
     */
    public List<TimeSlotEntity> findByExperimentId(@NonNull UUID experimentId) {
        return timeSlotRepository.findByExperimentIdOrderByStartTimeAsc(experimentId);
    }

    /**
     * 根據實驗 ID 查詢可報名的時段，依開始時間升冪排序。
     *
     * @param experimentId 實驗 ID
     * @return 可報名的時段列表
     */
    public List<TimeSlotEntity> findAvailableByExperimentId(@NonNull UUID experimentId) {
        return timeSlotRepository.findByExperimentIdAndStatusOrderByStartTimeAsc(
                experimentId, TimeSlotStatus.AVAILABLE);
    }

    /**
     * 儲存時段。
     *
     * @param timeSlotEntity 時段 Entity
     * @return 儲存後的時段 Entity
     */
    public TimeSlotEntity save(@NonNull TimeSlotEntity timeSlotEntity) {
        return timeSlotRepository.save(timeSlotEntity);
    }

    /**
     * 批次儲存時段。
     *
     * @param timeSlotEntities 時段 Entity 列表
     * @return 儲存後的時段 Entity 列表
     */
    public List<TimeSlotEntity> saveAll(@NonNull List<TimeSlotEntity> timeSlotEntities) {
        return timeSlotRepository.saveAll(timeSlotEntities);
    }
}

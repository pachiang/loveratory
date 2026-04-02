package com.loveratory.slot.repository;

import com.loveratory.slot.entity.TimeSlotEntity;
import com.loveratory.slot.entity.TimeSlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

/**
 * 時段 Repository。
 */
public interface TimeSlotRepository extends JpaRepository<TimeSlotEntity, UUID>,
        JpaSpecificationExecutor<TimeSlotEntity> {

    /**
     * 根據實驗 ID 查詢時段，依開始時間升冪排序。
     *
     * @param experimentId 實驗 ID
     * @return 時段列表
     */
    List<TimeSlotEntity> findByExperimentIdOrderByStartTimeAsc(UUID experimentId);

    /**
     * 根據實驗 ID 與狀態查詢時段，依開始時間升冪排序。
     *
     * @param experimentId 實驗 ID
     * @param status       時段狀態
     * @return 時段列表
     */
    List<TimeSlotEntity> findByExperimentIdAndStatusOrderByStartTimeAsc(UUID experimentId, TimeSlotStatus status);
}

package com.loveratory.registration.repository;

import com.loveratory.registration.entity.RegistrationEntity;
import com.loveratory.registration.entity.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 報名 Repository。
 */
public interface RegistrationRepository extends JpaRepository<RegistrationEntity, UUID>,
        JpaSpecificationExecutor<RegistrationEntity> {

    /**
     * 根據時段 ID 查詢報名，依報名時間升冪排序。
     *
     * @param timeSlotId 時段 ID
     * @return 報名列表
     */
    List<RegistrationEntity> findByTimeSlotIdOrderByRegisteredAtAsc(UUID timeSlotId);

    /**
     * 根據多個時段 ID 查詢報名，依報名時間升冪排序。
     *
     * @param timeSlotIds 時段 ID 列表
     * @return 報名列表
     */
    List<RegistrationEntity> findByTimeSlotIdInOrderByRegisteredAtAsc(List<UUID> timeSlotIds);

    /**
     * 根據取消 token 查詢報名。
     *
     * @param cancelToken 取消 token
     * @return 報名 Optional
     */
    Optional<RegistrationEntity> findByCancelToken(String cancelToken);

    /**
     * 檢查指定時段中是否存在同一 Email 且狀態為指定值的報名。
     *
     * @param timeSlotIds 時段 ID 列表
     * @param email       受試者 Email
     * @param status      報名狀態
     * @return 是否存在
     */
    boolean existsByTimeSlotIdInAndParticipantEmailAndStatus(List<UUID> timeSlotIds, String email, RegistrationStatus status);
}

package com.loveratory.registration.manager;

import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.registration.entity.RegistrationEntity;
import com.loveratory.registration.entity.RegistrationStatus;
import com.loveratory.registration.repository.RegistrationRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 報名資料存取管理器。
 * 封裝 RegistrationEntity 的所有 CRUD 操作。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RegistrationManager {

    private final RegistrationRepository registrationRepository;

    /**
     * 根據 ID 查詢報名，找不到時拋出 BusinessException。
     *
     * @param registrationId 報名 ID
     * @return 報名 Entity
     */
    public RegistrationEntity findByIdOrThrow(@NonNull UUID registrationId) {
        return registrationRepository.findById(registrationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND));
    }

    /**
     * 根據取消 token 查詢報名。
     *
     * @param cancelToken 取消 token
     * @return 報名 Optional
     */
    public Optional<RegistrationEntity> findByCancelToken(@NonNull String cancelToken) {
        return registrationRepository.findByCancelToken(cancelToken);
    }

    /**
     * 根據取消 token 查詢報名，找不到時拋出 BusinessException。
     *
     * @param cancelToken 取消 token
     * @return 報名 Entity
     */
    public RegistrationEntity findByCancelTokenOrThrow(@NonNull String cancelToken) {
        return registrationRepository.findByCancelToken(cancelToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND));
    }

    /**
     * 根據時段 ID 查詢報名列表。
     *
     * @param timeSlotId 時段 ID
     * @return 報名列表
     */
    public List<RegistrationEntity> findByTimeSlotId(@NonNull UUID timeSlotId) {
        return registrationRepository.findByTimeSlotIdOrderByRegisteredAtAsc(timeSlotId);
    }

    /**
     * 根據多個時段 ID 查詢報名列表。
     *
     * @param timeSlotIds 時段 ID 列表
     * @return 報名列表
     */
    public List<RegistrationEntity> findByTimeSlotIds(@NonNull List<UUID> timeSlotIds) {
        return registrationRepository.findByTimeSlotIdInOrderByRegisteredAtAsc(timeSlotIds);
    }

    /**
     * 檢查指定時段中是否存在同一 Email 的已確認報名。
     *
     * @param slotIds 時段 ID 列表
     * @param email   受試者 Email
     * @return 是否存在已確認的報名
     */
    public boolean existsConfirmedRegistration(@NonNull List<UUID> slotIds, @NonNull String email) {
        return registrationRepository.existsByTimeSlotIdInAndParticipantEmailAndStatus(
                slotIds, email, RegistrationStatus.CONFIRMED);
    }

    /**
     * 儲存報名。
     *
     * @param registrationEntity 報名 Entity
     * @return 儲存後的報名 Entity
     */
    public RegistrationEntity save(@NonNull RegistrationEntity registrationEntity) {
        return registrationRepository.save(registrationEntity);
    }
}

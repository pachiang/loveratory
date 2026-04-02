package com.loveratory.lab.manager;

import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.lab.entity.LabInvitationEntity;
import com.loveratory.lab.entity.LabInvitationStatus;
import com.loveratory.lab.repository.LabInvitationRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 實驗室邀請資料存取管理器。
 * 封裝 LabInvitationEntity 的所有 CRUD 操作。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class LabInvitationManager {

    private final LabInvitationRepository labInvitationRepository;

    /**
     * 根據邀請 Token 查詢邀請。
     *
     * @param token 邀請 Token
     * @return 邀請 Optional
     */
    public Optional<LabInvitationEntity> findByToken(@NonNull String token) {
        return labInvitationRepository.findByToken(token);
    }

    /**
     * 根據邀請 Token 查詢邀請，找不到時拋出 BusinessException。
     *
     * @param token 邀請 Token
     * @return 邀請 Entity
     */
    public LabInvitationEntity findByTokenOrThrow(@NonNull String token) {
        return labInvitationRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVITATION_NOT_FOUND));
    }

    /**
     * 根據實驗室 ID 查詢邀請列表。
     *
     * @param labId 實驗室 ID
     * @return 邀請列表（依建立時間降序）
     */
    public List<LabInvitationEntity> findByLabId(@NonNull UUID labId) {
        return labInvitationRepository.findByLabIdOrderByCreatedAtDesc(labId);
    }

    /**
     * 檢查指定實驗室是否存在指定 Email 的待處理邀請。
     *
     * @param labId 實驗室 ID
     * @param email 被邀請者 Email
     * @return 是否存在待處理邀請
     */
    public boolean existsPendingInvitation(@NonNull UUID labId, @NonNull String email) {
        return labInvitationRepository.existsByLabIdAndEmailAndStatus(labId, email, LabInvitationStatus.PENDING);
    }

    /**
     * 儲存實驗室邀請。
     *
     * @param labInvitationEntity 邀請 Entity
     * @return 儲存後的邀請 Entity
     */
    public LabInvitationEntity save(@NonNull LabInvitationEntity labInvitationEntity) {
        return labInvitationRepository.save(labInvitationEntity);
    }
}

package com.loveratory.lab.repository;

import com.loveratory.lab.entity.LabInvitationEntity;
import com.loveratory.lab.entity.LabInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 實驗室邀請 Repository。
 */
public interface LabInvitationRepository extends JpaRepository<LabInvitationEntity, UUID>,
        JpaSpecificationExecutor<LabInvitationEntity> {

    /**
     * 根據邀請 Token 查詢邀請。
     *
     * @param token 邀請 Token
     * @return 邀請 Optional
     */
    Optional<LabInvitationEntity> findByToken(String token);

    /**
     * 根據實驗室 ID 查詢邀請列表，依建立時間降序排列。
     *
     * @param labId 實驗室 ID
     * @return 邀請列表
     */
    List<LabInvitationEntity> findByLabIdOrderByCreatedAtDesc(UUID labId);

    /**
     * 檢查指定實驗室是否存在指定 Email 與狀態的邀請。
     *
     * @param labId  實驗室 ID
     * @param email  被邀請者 Email
     * @param status 邀請狀態
     * @return 是否存在
     */
    boolean existsByLabIdAndEmailAndStatus(UUID labId, String email, LabInvitationStatus status);
}

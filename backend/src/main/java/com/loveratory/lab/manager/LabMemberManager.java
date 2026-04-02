package com.loveratory.lab.manager;

import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.lab.entity.LabMemberEntity;
import com.loveratory.lab.entity.LabMemberStatus;
import com.loveratory.lab.repository.LabMemberRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 實驗室成員資料存取管理器。
 * 封裝 LabMemberEntity 的所有 CRUD 操作。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class LabMemberManager {

    private final LabMemberRepository labMemberRepository;

    /**
     * 根據實驗室 ID 與使用者 ID 查詢成員。
     *
     * @param labId  實驗室 ID
     * @param userId 使用者 ID
     * @return 成員 Optional
     */
    public Optional<LabMemberEntity> findByLabIdAndUserId(@NonNull UUID labId,
                                                          @NonNull UUID userId) {
        return labMemberRepository.findByLabIdAndUserId(labId, userId);
    }

    /**
     * 根據實驗室 ID 與使用者 ID 查詢成員，找不到時拋出 BusinessException。
     *
     * @param labId  實驗室 ID
     * @param userId 使用者 ID
     * @return 成員 Entity
     */
    public LabMemberEntity findByLabIdAndUserIdOrThrow(@NonNull UUID labId,
                                                       @NonNull UUID userId) {
        return labMemberRepository.findByLabIdAndUserId(labId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LAB_MEMBER_NOT_FOUND));
    }

    /**
     * 查詢實驗室的所有啟用中成員。
     *
     * @param labId 實驗室 ID
     * @return 啟用中成員列表
     */
    public List<LabMemberEntity> findActiveMembers(@NonNull UUID labId) {
        return labMemberRepository.findByLabIdAndStatus(labId, LabMemberStatus.ACTIVE);
    }

    /**
     * 查詢使用者所屬的所有啟用中實驗室成員關聯。
     *
     * @param userId 使用者 ID
     * @return 成員關聯列表
     */
    public List<LabMemberEntity> findLabsByUserId(@NonNull UUID userId) {
        return labMemberRepository.findByUserIdAndStatus(userId, LabMemberStatus.ACTIVE);
    }

    /**
     * 檢查使用者是否為實驗室的啟用中成員。
     *
     * @param labId  實驗室 ID
     * @param userId 使用者 ID
     * @return 是否為啟用中成員
     */
    public boolean existsActiveMember(@NonNull UUID labId, @NonNull UUID userId) {
        return labMemberRepository.existsByLabIdAndUserIdAndStatus(labId, userId, LabMemberStatus.ACTIVE);
    }

    /**
     * 儲存實驗室成員。
     *
     * @param labMemberEntity 成員 Entity
     * @return 儲存後的成員 Entity
     */
    public LabMemberEntity save(@NonNull LabMemberEntity labMemberEntity) {
        return labMemberRepository.save(labMemberEntity);
    }
}

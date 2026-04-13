package com.loveratory.lab.usecase;

import com.loveratory.auth.entity.UserEntity;
import com.loveratory.auth.manager.UserManager;
import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.common.util.SecurityUtil;
import com.loveratory.lab.dto.request.LabMemberRoleUpdateRequest;
import com.loveratory.lab.dto.response.LabMemberResponse;
import com.loveratory.lab.entity.LabMemberEntity;
import com.loveratory.lab.entity.LabMemberRole;
import com.loveratory.lab.entity.LabMemberStatus;
import com.loveratory.lab.manager.LabMemberManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 實驗室成員業務邏輯。
 * 處理成員查詢、角色變更與移除等操作。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class LabMemberUseCase {

    private final LabMemberManager labMemberManager;
    private final UserManager userManager;

    /**
     * 查詢實驗室的啟用中成員列表。
     * 需要當前使用者為該實驗室的啟用中成員。
     *
     * @param labId 實驗室 ID
     * @return 成員列表
     */
    @Transactional(readOnly = true)
    public List<LabMemberResponse> findMembers(@NonNull UUID labId) {
        verifyActiveMember(labId);

        List<LabMemberEntity> activeMembers = labMemberManager.findActiveMembers(labId);

        return activeMembers.stream()
                .map(memberEntity -> {
                    UserEntity userEntity = userManager.findByIdOrThrow(memberEntity.getUserId());
                    return LabMemberResponse.of(memberEntity, userEntity);
                })
                .toList();
    }

    /**
     * 更新實驗室成員角色。
     * 僅限實驗室管理員操作。
     *
     * @param labId        實驗室 ID
     * @param targetUserId 目標成員使用者 ID
     * @param request      角色更新請求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMemberRole(@NonNull UUID labId,
                                 @NonNull UUID targetUserId,
                                 @NonNull LabMemberRoleUpdateRequest request) {
        verifyLabAdmin(labId);

        LabMemberEntity targetMember = labMemberManager.findByLabIdAndUserIdOrThrow(labId, targetUserId);
        targetMember.setRole(request.getRole());
        labMemberManager.save(targetMember);
    }

    /**
     * 移除實驗室成員。
     * 僅限實驗室管理員操作。
     *
     * @param labId        實驗室 ID
     * @param targetUserId 目標成員使用者 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(@NonNull UUID labId, @NonNull UUID targetUserId) {
        verifyLabAdmin(labId);

        LabMemberEntity targetMember = labMemberManager.findByLabIdAndUserIdOrThrow(labId, targetUserId);
        targetMember.setStatus(LabMemberStatus.REMOVED);
        labMemberManager.save(targetMember);
    }

    /**
     * 驗證當前使用者為實驗室管理員。
     *
     * @param labId 實驗室 ID
     * @return 當前使用者的成員 Entity
     */
    private LabMemberEntity verifyLabAdmin(UUID labId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        LabMemberEntity member = labMemberManager.findActiveByLabIdAndUserIdOrThrow(labId, currentUserId);
        if (member.getRole() != LabMemberRole.LAB_ADMIN) {
            throw new BusinessException(ErrorCode.NOT_LAB_ADMIN);
        }
        return member;
    }

    /**
     * 驗證當前使用者為實驗室啟用中成員。
     *
     * @param labId 實驗室 ID
     */
    private void verifyActiveMember(UUID labId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (!labMemberManager.existsActiveMember(labId, currentUserId)) {
            throw new BusinessException(ErrorCode.LAB_MEMBER_NOT_FOUND);
        }
    }
}

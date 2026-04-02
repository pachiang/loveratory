package com.loveratory.lab.usecase;

import com.loveratory.auth.entity.UserEntity;
import com.loveratory.auth.manager.UserManager;
import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.common.util.SecurityUtil;
import com.loveratory.lab.dto.request.LabInvitationCreateRequest;
import com.loveratory.lab.dto.response.LabInvitationResponse;
import com.loveratory.lab.entity.LabEntity;
import com.loveratory.lab.entity.LabInvitationEntity;
import com.loveratory.lab.entity.LabInvitationStatus;
import com.loveratory.lab.entity.LabMemberEntity;
import com.loveratory.lab.entity.LabMemberRole;
import com.loveratory.lab.entity.LabMemberStatus;
import com.loveratory.lab.manager.LabInvitationManager;
import com.loveratory.lab.manager.LabManager;
import com.loveratory.lab.manager.LabMemberManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 實驗室邀請業務邏輯。
 * 處理邀請的建立、查詢、撤銷與接受等操作。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class LabInvitationUseCase {

    private final LabInvitationManager labInvitationManager;
    private final LabMemberManager labMemberManager;
    private final LabManager labManager;
    private final UserManager userManager;

    /**
     * 建立實驗室邀請。
     * 僅限實驗室管理員操作。邀請有效期為 7 天。
     *
     * @param labId   實驗室 ID
     * @param request 建立邀請請求
     * @return 邀請回應
     */
    @Transactional(rollbackFor = Exception.class)
    public LabInvitationResponse createInvitation(@NonNull UUID labId,
                                                  @NonNull LabInvitationCreateRequest request) {
        LabMemberEntity currentMember = verifyLabAdmin(labId);

        if (labInvitationManager.existsPendingInvitation(labId, request.getEmail())) {
            throw new BusinessException(ErrorCode.INVITATION_ALREADY_EXISTS);
        }

        // 檢查目標是否已是成員
        Optional<UserEntity> targetUser = userManager.findByEmail(request.getEmail());
        if (targetUser.isPresent()
                && labMemberManager.existsActiveMember(labId, targetUser.get().getId())) {
            throw new BusinessException(ErrorCode.LAB_MEMBER_ALREADY_EXISTS);
        }

        LabInvitationEntity invitationEntity = new LabInvitationEntity();
        invitationEntity.setLabId(labId);
        invitationEntity.setEmail(request.getEmail());
        invitationEntity.setToken(UUID.randomUUID().toString());
        invitationEntity.setInvitedBy(currentMember.getUserId());
        invitationEntity.setStatus(LabInvitationStatus.PENDING);
        invitationEntity.setExpiresAt(ZonedDateTime.now().plusDays(7));

        LabInvitationEntity savedInvitationEntity = labInvitationManager.save(invitationEntity);

        UserEntity inviter = userManager.findByIdOrThrow(currentMember.getUserId());
        return LabInvitationResponse.of(savedInvitationEntity, inviter.getName());
    }

    /**
     * 查詢實驗室的邀請列表。
     * 僅限實驗室管理員操作。
     *
     * @param labId 實驗室 ID
     * @return 邀請列表
     */
    @Transactional(readOnly = true)
    public List<LabInvitationResponse> findInvitations(@NonNull UUID labId) {
        verifyLabAdmin(labId);

        List<LabInvitationEntity> invitations = labInvitationManager.findByLabId(labId);

        return invitations.stream()
                .map(invitationEntity -> {
                    UserEntity inviter = userManager.findByIdOrThrow(invitationEntity.getInvitedBy());
                    return LabInvitationResponse.of(invitationEntity, inviter.getName());
                })
                .toList();
    }

    /**
     * 撤銷實驗室邀請。
     * 僅限實驗室管理員操作，將邀請狀態設為已過期。
     *
     * @param labId        實驗室 ID
     * @param invitationId 邀請 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokeInvitation(@NonNull UUID labId, @NonNull UUID invitationId) {
        verifyLabAdmin(labId);

        LabInvitationEntity invitationEntity = labInvitationManager
                .findByToken(invitationId.toString())
                .orElseGet(() -> {
                    // 嘗試以 ID 查找
                    return labInvitationManager.findByLabId(labId).stream()
                            .filter(invitation -> invitation.getId().equals(invitationId))
                            .findFirst()
                            .orElseThrow(() -> new BusinessException(ErrorCode.INVITATION_NOT_FOUND));
                });

        invitationEntity.setStatus(LabInvitationStatus.EXPIRED);
        labInvitationManager.save(invitationEntity);
    }

    /**
     * 根據 Token 查詢邀請資訊。
     * 此為公開 API，不需登入即可查看。
     *
     * @param token 邀請 Token
     * @return 邀請回應
     */
    @Transactional(readOnly = true)
    public LabInvitationResponse findInvitationByToken(@NonNull String token) {
        LabInvitationEntity invitationEntity = labInvitationManager.findByTokenOrThrow(token);
        UserEntity inviter = userManager.findByIdOrThrow(invitationEntity.getInvitedBy());
        return LabInvitationResponse.of(invitationEntity, inviter.getName());
    }

    /**
     * 接受實驗室邀請。
     * 需要使用者已登入。檢查邀請狀態與有效期，加入成員並更新邀請狀態。
     *
     * @param token 邀請 Token
     */
    @Transactional(rollbackFor = Exception.class)
    public void acceptInvitation(@NonNull String token) {
        LabInvitationEntity invitationEntity = labInvitationManager.findByTokenOrThrow(token);

        if (invitationEntity.getStatus() != LabInvitationStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVITATION_ALREADY_ACCEPTED);
        }

        if (invitationEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new BusinessException(ErrorCode.INVITATION_EXPIRED);
        }

        UUID currentUserId = SecurityUtil.getCurrentUserId();

        if (labMemberManager.existsActiveMember(invitationEntity.getLabId(), currentUserId)) {
            throw new BusinessException(ErrorCode.LAB_MEMBER_ALREADY_EXISTS);
        }

        LabMemberEntity memberEntity = new LabMemberEntity();
        memberEntity.setLabId(invitationEntity.getLabId());
        memberEntity.setUserId(currentUserId);
        memberEntity.setRole(LabMemberRole.LAB_MEMBER);
        memberEntity.setStatus(LabMemberStatus.ACTIVE);
        labMemberManager.save(memberEntity);

        invitationEntity.setStatus(LabInvitationStatus.ACCEPTED);
        invitationEntity.setAcceptedAt(ZonedDateTime.now());
        labInvitationManager.save(invitationEntity);
    }

    /**
     * 驗證當前使用者為實驗室管理員。
     *
     * @param labId 實驗室 ID
     * @return 當前使用者的成員 Entity
     */
    private LabMemberEntity verifyLabAdmin(UUID labId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        LabMemberEntity member = labMemberManager.findByLabIdAndUserIdOrThrow(labId, currentUserId);
        if (member.getRole() != LabMemberRole.LAB_ADMIN) {
            throw new BusinessException(ErrorCode.NOT_LAB_ADMIN);
        }
        return member;
    }
}

package com.loveratory.project.usecase;

import com.loveratory.auth.entity.UserEntity;
import com.loveratory.auth.manager.UserManager;
import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.common.util.SecurityUtil;
import com.loveratory.lab.entity.LabMemberRole;
import com.loveratory.lab.manager.LabMemberManager;
import com.loveratory.project.dto.request.InvestigatorAddRequest;
import com.loveratory.project.dto.response.InvestigatorResponse;
import com.loveratory.project.entity.ProjectEntity;
import com.loveratory.project.entity.ProjectInvestigatorEntity;
import com.loveratory.project.entity.ProjectInvestigatorStatus;
import com.loveratory.project.manager.ProjectInvestigatorManager;
import com.loveratory.project.manager.ProjectManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 專案主持人業務邏輯。
 * 處理主持人的查詢、新增與移除操作。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectInvestigatorUseCase {

    private final ProjectInvestigatorManager projectInvestigatorManager;
    private final ProjectManager projectManager;
    private final UserManager userManager;
    private final LabMemberManager labMemberManager;

    /**
     * 查詢專案的主持人列表。
     * 需為專案主持人或實驗室管理員。
     *
     * @param projectId 專案 ID
     * @return 主持人回應列表
     */
    @Transactional(readOnly = true)
    public List<InvestigatorResponse> findInvestigators(@NonNull UUID projectId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        ProjectEntity project = projectManager.findByIdOrThrow(projectId);

        // 驗證權限：主持人或 LAB_ADMIN
        verifyProjectAccess(project, currentUserId);

        List<ProjectInvestigatorEntity> investigators =
                projectInvestigatorManager.findActiveInvestigators(projectId);

        return investigators.stream()
                .map(inv -> {
                    UserEntity user = userManager.findByIdOrThrow(inv.getUserId());
                    return InvestigatorResponse.of(inv, user);
                })
                .toList();
    }

    /**
     * 新增專案主持人。
     * 需為專案的啟用中主持人，且目標使用者需為實驗室的啟用中成員。
     *
     * @param projectId 專案 ID
     * @param request   新增主持人請求
     * @return 主持人回應
     */
    @Transactional(rollbackFor = Exception.class)
    public InvestigatorResponse addInvestigator(@NonNull UUID projectId,
                                                @NonNull InvestigatorAddRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        ProjectEntity project = projectManager.findByIdOrThrow(projectId);

        // 驗證當前使用者是啟用中主持人
        verifyActiveInvestigator(projectId, currentUserId);

        UUID targetUserId = request.getUserId();

        // 驗證目標使用者是實驗室的啟用中成員
        if (!labMemberManager.existsActiveMember(project.getLabId(), targetUserId)) {
            throw new BusinessException(ErrorCode.NOT_LAB_MEMBER);
        }

        // 檢查是否已是主持人
        if (projectInvestigatorManager.existsActiveInvestigator(projectId, targetUserId)) {
            throw new BusinessException(ErrorCode.INVESTIGATOR_ALREADY_EXISTS);
        }

        // 建立主持人
        ProjectInvestigatorEntity investigatorEntity = new ProjectInvestigatorEntity();
        investigatorEntity.setProjectId(projectId);
        investigatorEntity.setUserId(targetUserId);
        investigatorEntity.setAddedBy(currentUserId);
        investigatorEntity.setStatus(ProjectInvestigatorStatus.ACTIVE);

        ProjectInvestigatorEntity savedInvestigator = projectInvestigatorManager.save(investigatorEntity);

        UserEntity targetUser = userManager.findByIdOrThrow(targetUserId);
        return InvestigatorResponse.of(savedInvestigator, targetUser);
    }

    /**
     * 移除專案主持人。
     * 需為專案的啟用中主持人，且專案至少需保留一位主持人。
     *
     * @param projectId    專案 ID
     * @param targetUserId 要移除的使用者 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeInvestigator(@NonNull UUID projectId, @NonNull UUID targetUserId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        projectManager.findByIdOrThrow(projectId);

        // 驗證當前使用者是啟用中主持人
        verifyActiveInvestigator(projectId, currentUserId);

        // 檢查至少保留一位主持人
        long activeCount = projectInvestigatorManager.countActiveInvestigators(projectId);
        if (activeCount <= 1) {
            throw new BusinessException(ErrorCode.LAST_INVESTIGATOR_CANNOT_REMOVE);
        }

        // 查詢目標主持人
        ProjectInvestigatorEntity targetInvestigator = projectInvestigatorManager
                .findByProjectIdAndUserId(projectId, targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_PROJECT_INVESTIGATOR));

        targetInvestigator.setStatus(ProjectInvestigatorStatus.REMOVED);
        projectInvestigatorManager.save(targetInvestigator);
    }

    /**
     * 驗證使用者對專案的存取權限。
     * 需為專案主持人或實驗室管理員。
     */
    private void verifyProjectAccess(ProjectEntity project, UUID userId) {
        boolean isInvestigator = projectInvestigatorManager.existsActiveInvestigator(project.getId(), userId);
        if (!isInvestigator) {
            // 檢查是否為 LAB_ADMIN
            var membership = labMemberManager.findByLabIdAndUserIdOrThrow(
                    project.getLabId(), userId);
            if (membership.getRole() != LabMemberRole.LAB_ADMIN) {
                throw new BusinessException(ErrorCode.NOT_PROJECT_INVESTIGATOR);
            }
        }
    }

    /**
     * 驗證使用者是否為專案的啟用中主持人。
     */
    private void verifyActiveInvestigator(UUID projectId, UUID userId) {
        if (!projectInvestigatorManager.existsActiveInvestigator(projectId, userId)) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_INVESTIGATOR);
        }
    }
}

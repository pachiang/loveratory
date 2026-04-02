package com.loveratory.project.usecase;

import com.loveratory.auth.entity.UserEntity;
import com.loveratory.auth.manager.UserManager;
import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.common.util.SecurityUtil;
import com.loveratory.lab.entity.LabMemberEntity;
import com.loveratory.lab.entity.LabMemberRole;
import com.loveratory.lab.manager.LabMemberManager;
import com.loveratory.project.dto.request.ProjectCreateRequest;
import com.loveratory.project.dto.request.ProjectUpdateRequest;
import com.loveratory.project.dto.response.InvestigatorResponse;
import com.loveratory.project.dto.response.ProjectDetailResponse;
import com.loveratory.project.dto.response.ProjectSummaryResponse;
import com.loveratory.project.entity.ProjectEntity;
import com.loveratory.project.entity.ProjectInvestigatorEntity;
import com.loveratory.project.entity.ProjectInvestigatorStatus;
import com.loveratory.project.entity.ProjectStatus;
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
 * 專案業務邏輯。
 * 處理專案的建立、查詢、更新與封存等操作。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectUseCase {

    private final ProjectManager projectManager;
    private final ProjectInvestigatorManager projectInvestigatorManager;
    private final UserManager userManager;
    private final LabMemberManager labMemberManager;

    /**
     * 建立專案。
     * 建立後狀態為 DRAFT，建立者自動成為主持人。
     *
     * @param labId   實驗室 ID
     * @param request 建立專案請求
     * @return 專案詳情回應
     */
    @Transactional(rollbackFor = Exception.class)
    public ProjectDetailResponse createProject(@NonNull UUID labId, @NonNull ProjectCreateRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        // 驗證當前使用者是實驗室的啟用中成員
        if (!labMemberManager.existsActiveMember(labId, currentUserId)) {
            throw new BusinessException(ErrorCode.NOT_LAB_MEMBER);
        }

        // 建立專案
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setLabId(labId);
        projectEntity.setName(request.getName());
        projectEntity.setDescription(request.getDescription());
        projectEntity.setCreatedBy(currentUserId);
        projectEntity.setStatus(ProjectStatus.DRAFT);

        ProjectEntity savedProject = projectManager.save(projectEntity);

        // 建立者自動成為主持人
        ProjectInvestigatorEntity investigatorEntity = new ProjectInvestigatorEntity();
        investigatorEntity.setProjectId(savedProject.getId());
        investigatorEntity.setUserId(currentUserId);
        investigatorEntity.setAddedBy(currentUserId);
        investigatorEntity.setStatus(ProjectInvestigatorStatus.ACTIVE);

        projectInvestigatorManager.save(investigatorEntity);

        // 組裝回應
        UserEntity creator = userManager.findByIdOrThrow(currentUserId);
        List<InvestigatorResponse> investigators = List.of(
                InvestigatorResponse.of(investigatorEntity, creator)
        );

        return ProjectDetailResponse.fromEntity(savedProject, creator.getName(), investigators);
    }

    /**
     * 查詢實驗室的專案列表。
     * LAB_ADMIN 可看到所有專案，一般成員只能看到自己是主持人的專案。
     *
     * @param labId 實驗室 ID
     * @return 專案摘要列表
     */
    @Transactional(readOnly = true)
    public List<ProjectSummaryResponse> findProjects(@NonNull UUID labId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        LabMemberEntity membership = labMemberManager.findByLabIdAndUserIdOrThrow(labId, currentUserId);

        List<ProjectEntity> projects;

        if (membership.getRole() == LabMemberRole.LAB_ADMIN) {
            // LAB_ADMIN 可看到所有專案
            projects = projectManager.findByLabId(labId);
        } else {
            // 一般成員只看到自己是啟用中主持人的專案
            projects = projectManager.findByLabId(labId).stream()
                    .filter(project -> projectInvestigatorManager.existsActiveInvestigator(
                            project.getId(), currentUserId))
                    .toList();
        }

        return projects.stream()
                .map(ProjectSummaryResponse::fromEntity)
                .toList();
    }

    /**
     * 查詢專案詳情。
     * 需為專案主持人或實驗室管理員。
     *
     * @param projectId 專案 ID
     * @return 專案詳情回應
     */
    @Transactional(readOnly = true)
    public ProjectDetailResponse findProjectDetail(@NonNull UUID projectId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        ProjectEntity project = projectManager.findByIdOrThrow(projectId);

        // 驗證權限：主持人或 LAB_ADMIN
        verifyProjectAccess(project, currentUserId);

        return buildProjectDetailResponse(project);
    }

    /**
     * 更新專案。
     * 需為專案的啟用中主持人。
     *
     * @param projectId 專案 ID
     * @param request   更新專案請求
     * @return 專案詳情回應
     */
    @Transactional(rollbackFor = Exception.class)
    public ProjectDetailResponse updateProject(@NonNull UUID projectId, @NonNull ProjectUpdateRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        ProjectEntity project = projectManager.findByIdOrThrow(projectId);

        // 驗證當前使用者是啟用中主持人
        verifyActiveInvestigator(projectId, currentUserId);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());

        ProjectEntity savedProject = projectManager.save(project);

        return buildProjectDetailResponse(savedProject);
    }

    /**
     * 封存專案。
     * 需為專案的啟用中主持人。
     *
     * @param projectId 專案 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void archiveProject(@NonNull UUID projectId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        ProjectEntity project = projectManager.findByIdOrThrow(projectId);

        // 驗證當前使用者是啟用中主持人
        verifyActiveInvestigator(projectId, currentUserId);

        project.setStatus(ProjectStatus.ARCHIVED);
        projectManager.save(project);
    }

    /**
     * 驗證使用者對專案的存取權限。
     * 需為專案主持人或實驗室管理員。
     */
    private void verifyProjectAccess(ProjectEntity project, UUID userId) {
        boolean isInvestigator = projectInvestigatorManager.existsActiveInvestigator(project.getId(), userId);
        if (!isInvestigator) {
            // 檢查是否為 LAB_ADMIN
            LabMemberEntity membership = labMemberManager.findByLabIdAndUserIdOrThrow(
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

    /**
     * 組裝專案詳情回應。
     */
    private ProjectDetailResponse buildProjectDetailResponse(ProjectEntity project) {
        UserEntity creator = userManager.findByIdOrThrow(project.getCreatedBy());

        List<ProjectInvestigatorEntity> investigatorEntities =
                projectInvestigatorManager.findActiveInvestigators(project.getId());

        List<InvestigatorResponse> investigators = investigatorEntities.stream()
                .map(inv -> {
                    UserEntity user = userManager.findByIdOrThrow(inv.getUserId());
                    return InvestigatorResponse.of(inv, user);
                })
                .toList();

        return ProjectDetailResponse.fromEntity(project, creator.getName(), investigators);
    }
}

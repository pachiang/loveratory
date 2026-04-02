package com.loveratory.experiment.usecase;

import com.loveratory.auth.entity.UserEntity;
import com.loveratory.auth.manager.UserManager;
import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.common.util.SecurityUtil;
import com.loveratory.experiment.dto.internal.FormConfig;
import com.loveratory.experiment.dto.internal.NotificationConfig;
import com.loveratory.experiment.dto.request.ExperimentCreateRequest;
import com.loveratory.experiment.dto.request.ExperimentStatusUpdateRequest;
import com.loveratory.experiment.dto.request.ExperimentUpdateRequest;
import com.loveratory.experiment.dto.response.ExperimentDetailResponse;
import com.loveratory.experiment.dto.response.ExperimentSummaryResponse;
import com.loveratory.experiment.entity.ExperimentEntity;
import com.loveratory.experiment.entity.ExperimentStatus;
import com.loveratory.experiment.manager.ExperimentManager;
import com.loveratory.lab.entity.LabMemberRole;
import com.loveratory.lab.entity.LabMemberStatus;
import com.loveratory.lab.manager.LabMemberManager;
import com.loveratory.project.entity.ProjectEntity;
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
 * 實驗業務邏輯。
 * 處理實驗的建立、查詢、更新、狀態變更與設定管理等操作。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExperimentUseCase {

    private final ExperimentManager experimentManager;
    private final ProjectManager projectManager;
    private final ProjectInvestigatorManager projectInvestigatorManager;
    private final UserManager userManager;
    private final LabMemberManager labMemberManager;

    /**
     * 建立實驗。
     * 建立後狀態為 DRAFT，並使用預設的表單設定與通知設定。
     *
     * @param projectId 專案 ID
     * @param request   建立實驗請求
     * @return 實驗詳情回應
     */
    @Transactional(rollbackFor = Exception.class)
    public ExperimentDetailResponse createExperiment(@NonNull UUID projectId,
                                                     @NonNull ExperimentCreateRequest request) {
        ProjectEntity project = projectManager.findByIdOrThrow(projectId);
        verifyProjectInvestigator(project);

        if (experimentManager.existsBySlug(request.getSlug())) {
            throw new BusinessException(ErrorCode.EXPERIMENT_SLUG_ALREADY_EXISTS);
        }

        UUID currentUserId = SecurityUtil.getCurrentUserId();

        ExperimentEntity experiment = new ExperimentEntity();
        experiment.setProjectId(projectId);
        experiment.setName(request.getName());
        experiment.setDescription(request.getDescription());
        experiment.setLocation(request.getLocation());
        experiment.setDurationMinutes(request.getDurationMinutes());
        experiment.setMaxParticipantsPerSlot(request.getMaxParticipantsPerSlot());
        experiment.setSlug(request.getSlug());
        experiment.setStatus(ExperimentStatus.DRAFT);
        experiment.setAllowDuplicateEmail(request.getAllowDuplicateEmail());
        experiment.setAllowParticipantCancel(request.getAllowParticipantCancel());
        experiment.setFormConfig(FormConfig.createDefault());
        experiment.setNotificationConfig(NotificationConfig.createDefault());
        experiment.setCreatedBy(currentUserId);

        ExperimentEntity savedExperiment = experimentManager.save(experiment);

        UserEntity creator = userManager.findByIdOrThrow(currentUserId);
        return ExperimentDetailResponse.fromEntity(savedExperiment, creator.getName());
    }

    /**
     * 查詢專案下的所有實驗列表。
     *
     * @param projectId 專案 ID
     * @return 實驗摘要列表
     */
    @Transactional(readOnly = true)
    public List<ExperimentSummaryResponse> findExperiments(@NonNull UUID projectId) {
        ProjectEntity project = projectManager.findByIdOrThrow(projectId);
        verifyProjectInvestigator(project);

        List<ExperimentEntity> experiments = experimentManager.findByProjectId(projectId);
        return experiments.stream()
                .map(ExperimentSummaryResponse::fromEntity)
                .toList();
    }

    /**
     * 查詢實驗詳情。
     *
     * @param experimentId 實驗 ID
     * @return 實驗詳情回應
     */
    @Transactional(readOnly = true)
    public ExperimentDetailResponse findExperimentDetail(@NonNull UUID experimentId) {
        ExperimentEntity experiment = experimentManager.findByIdOrThrow(experimentId);
        ProjectEntity project = projectManager.findByIdOrThrow(experiment.getProjectId());
        verifyProjectInvestigator(project);

        UserEntity creator = userManager.findByIdOrThrow(experiment.getCreatedBy());
        return ExperimentDetailResponse.fromEntity(experiment, creator.getName());
    }

    /**
     * 更新實驗資訊。
     * 若實驗狀態為 OPEN，不可變更 allowParticipantCancel 設定。
     *
     * @param experimentId 實驗 ID
     * @param request      更新實驗請求
     * @return 實驗詳情回應
     */
    @Transactional(rollbackFor = Exception.class)
    public ExperimentDetailResponse updateExperiment(@NonNull UUID experimentId,
                                                     @NonNull ExperimentUpdateRequest request) {
        ExperimentEntity experiment = experimentManager.findByIdOrThrow(experimentId);
        ProjectEntity project = projectManager.findByIdOrThrow(experiment.getProjectId());
        verifyProjectInvestigator(project);

        if (experiment.getStatus() == ExperimentStatus.OPEN
                && request.getAllowParticipantCancel() != null
                && !request.getAllowParticipantCancel().equals(experiment.getAllowParticipantCancel())) {
            throw new BusinessException(ErrorCode.CANCEL_POLICY_LOCKED);
        }

        experiment.setName(request.getName());
        experiment.setDescription(request.getDescription());
        experiment.setLocation(request.getLocation());
        experiment.setDurationMinutes(request.getDurationMinutes());
        experiment.setMaxParticipantsPerSlot(request.getMaxParticipantsPerSlot());
        experiment.setAllowDuplicateEmail(request.getAllowDuplicateEmail());

        if (request.getAllowParticipantCancel() != null) {
            experiment.setAllowParticipantCancel(request.getAllowParticipantCancel());
        }

        ExperimentEntity savedExperiment = experimentManager.save(experiment);

        UserEntity creator = userManager.findByIdOrThrow(experiment.getCreatedBy());
        return ExperimentDetailResponse.fromEntity(savedExperiment, creator.getName());
    }

    /**
     * 更新實驗狀態。
     *
     * @param experimentId 實驗 ID
     * @param request      更新狀態請求
     * @return 實驗詳情回應
     */
    @Transactional(rollbackFor = Exception.class)
    public ExperimentDetailResponse updateExperimentStatus(@NonNull UUID experimentId,
                                                           @NonNull ExperimentStatusUpdateRequest request) {
        ExperimentEntity experiment = experimentManager.findByIdOrThrow(experimentId);
        ProjectEntity project = projectManager.findByIdOrThrow(experiment.getProjectId());
        verifyProjectInvestigator(project);

        experiment.setStatus(request.getStatus());

        ExperimentEntity savedExperiment = experimentManager.save(experiment);

        UserEntity creator = userManager.findByIdOrThrow(experiment.getCreatedBy());
        return ExperimentDetailResponse.fromEntity(savedExperiment, creator.getName());
    }

    /**
     * 更新報名表單設定。
     * Email 欄位必須保持鎖定、必填且顯示。
     *
     * @param experimentId 實驗 ID
     * @param formConfig   表單設定
     * @return 實驗詳情回應
     */
    @Transactional(rollbackFor = Exception.class)
    public ExperimentDetailResponse updateFormConfig(@NonNull UUID experimentId,
                                                     @NonNull FormConfig formConfig) {
        ExperimentEntity experiment = experimentManager.findByIdOrThrow(experimentId);
        ProjectEntity project = projectManager.findByIdOrThrow(experiment.getProjectId());
        verifyProjectInvestigator(project);

        ensureEmailFieldLocked(formConfig);

        experiment.setFormConfig(formConfig);

        ExperimentEntity savedExperiment = experimentManager.save(experiment);

        UserEntity creator = userManager.findByIdOrThrow(experiment.getCreatedBy());
        return ExperimentDetailResponse.fromEntity(savedExperiment, creator.getName());
    }

    /**
     * 更新通知設定。
     *
     * @param experimentId       實驗 ID
     * @param notificationConfig 通知設定
     * @return 實驗詳情回應
     */
    @Transactional(rollbackFor = Exception.class)
    public ExperimentDetailResponse updateNotificationConfig(@NonNull UUID experimentId,
                                                             @NonNull NotificationConfig notificationConfig) {
        ExperimentEntity experiment = experimentManager.findByIdOrThrow(experimentId);
        ProjectEntity project = projectManager.findByIdOrThrow(experiment.getProjectId());
        verifyProjectInvestigator(project);

        experiment.setNotificationConfig(notificationConfig);

        ExperimentEntity savedExperiment = experimentManager.save(experiment);

        UserEntity creator = userManager.findByIdOrThrow(experiment.getCreatedBy());
        return ExperimentDetailResponse.fromEntity(savedExperiment, creator.getName());
    }

    /**
     * 驗證當前使用者是否為專案主持人或所屬實驗室管理員。
     * 若兩者皆非，拋出 NOT_PROJECT_INVESTIGATOR 例外。
     *
     * @param project 專案 Entity
     */
    private void verifyProjectInvestigator(ProjectEntity project) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        boolean isInvestigator = projectInvestigatorManager.existsActiveInvestigator(
                project.getId(), currentUserId);
        if (isInvestigator) {
            return;
        }

        boolean isLabAdmin = labMemberManager.findByLabIdAndUserId(
                        project.getLabId(), currentUserId)
                .filter(member -> member.getStatus() == LabMemberStatus.ACTIVE)
                .filter(member -> member.getRole() == LabMemberRole.LAB_ADMIN)
                .isPresent();
        if (isLabAdmin) {
            return;
        }

        throw new BusinessException(ErrorCode.NOT_PROJECT_INVESTIGATOR);
    }

    /**
     * 確保 Email 欄位維持鎖定、必填且顯示。
     * 若表單設定中包含 email 欄位，強制設定其屬性。
     *
     * @param formConfig 表單設定
     */
    private void ensureEmailFieldLocked(FormConfig formConfig) {
        if (formConfig.getFields() == null) {
            return;
        }

        formConfig.getFields().stream()
                .filter(field -> "email".equals(field.getKey()))
                .forEach(field -> {
                    field.setVisible(true);
                    field.setRequired(true);
                    field.setLocked(true);
                });
    }
}

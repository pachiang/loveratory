package com.loveratory.project.manager;

import com.loveratory.project.entity.ProjectInvestigatorEntity;
import com.loveratory.project.entity.ProjectInvestigatorStatus;
import com.loveratory.project.repository.ProjectInvestigatorRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 專案主持人資料存取管理器。
 * 封裝 ProjectInvestigatorEntity 的所有 CRUD 操作。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ProjectInvestigatorManager {

    private final ProjectInvestigatorRepository projectInvestigatorRepository;

    /**
     * 查詢專案的啟用中主持人列表。
     *
     * @param projectId 專案 ID
     * @return 啟用中主持人列表
     */
    public List<ProjectInvestigatorEntity> findActiveInvestigators(@NonNull UUID projectId) {
        return projectInvestigatorRepository.findByProjectIdAndStatus(projectId, ProjectInvestigatorStatus.ACTIVE);
    }

    /**
     * 根據專案 ID 與使用者 ID 查詢主持人。
     *
     * @param projectId 專案 ID
     * @param userId    使用者 ID
     * @return 主持人 Optional
     */
    public Optional<ProjectInvestigatorEntity> findByProjectIdAndUserId(@NonNull UUID projectId,
                                                                        @NonNull UUID userId) {
        return projectInvestigatorRepository.findByProjectIdAndUserId(projectId, userId);
    }

    /**
     * 檢查使用者是否為專案的啟用中主持人。
     *
     * @param projectId 專案 ID
     * @param userId    使用者 ID
     * @return 是否為啟用中主持人
     */
    public boolean existsActiveInvestigator(@NonNull UUID projectId, @NonNull UUID userId) {
        return projectInvestigatorRepository.existsByProjectIdAndUserIdAndStatus(
                projectId, userId, ProjectInvestigatorStatus.ACTIVE);
    }

    /**
     * 統計專案的啟用中主持人數量。
     *
     * @param projectId 專案 ID
     * @return 啟用中主持人數量
     */
    public long countActiveInvestigators(@NonNull UUID projectId) {
        return projectInvestigatorRepository.countByProjectIdAndStatus(projectId, ProjectInvestigatorStatus.ACTIVE);
    }

    /**
     * 儲存專案主持人。
     *
     * @param entity 主持人 Entity
     * @return 儲存後的主持人 Entity
     */
    public ProjectInvestigatorEntity save(@NonNull ProjectInvestigatorEntity entity) {
        return projectInvestigatorRepository.save(entity);
    }
}

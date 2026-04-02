package com.loveratory.project.repository;

import com.loveratory.project.entity.ProjectInvestigatorEntity;
import com.loveratory.project.entity.ProjectInvestigatorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 專案主持人 Repository。
 */
public interface ProjectInvestigatorRepository extends JpaRepository<ProjectInvestigatorEntity, UUID>,
        JpaSpecificationExecutor<ProjectInvestigatorEntity> {

    /**
     * 根據專案 ID 與狀態查詢主持人列表。
     *
     * @param projectId 專案 ID
     * @param status    主持人狀態
     * @return 主持人列表
     */
    List<ProjectInvestigatorEntity> findByProjectIdAndStatus(UUID projectId, ProjectInvestigatorStatus status);

    /**
     * 根據專案 ID 與使用者 ID 查詢主持人。
     *
     * @param projectId 專案 ID
     * @param userId    使用者 ID
     * @return 主持人 Optional
     */
    Optional<ProjectInvestigatorEntity> findByProjectIdAndUserId(UUID projectId, UUID userId);

    /**
     * 檢查指定使用者是否為專案的指定狀態主持人。
     *
     * @param projectId 專案 ID
     * @param userId    使用者 ID
     * @param status    主持人狀態
     * @return 是否存在
     */
    boolean existsByProjectIdAndUserIdAndStatus(UUID projectId, UUID userId, ProjectInvestigatorStatus status);

    /**
     * 統計專案中指定狀態的主持人數量。
     *
     * @param projectId 專案 ID
     * @param status    主持人狀態
     * @return 主持人數量
     */
    long countByProjectIdAndStatus(UUID projectId, ProjectInvestigatorStatus status);
}

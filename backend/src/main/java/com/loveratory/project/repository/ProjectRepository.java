package com.loveratory.project.repository;

import com.loveratory.project.entity.ProjectEntity;
import com.loveratory.project.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

/**
 * 專案 Repository。
 */
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID>,
        JpaSpecificationExecutor<ProjectEntity> {

    /**
     * 根據實驗室 ID 與專案狀態查詢專案列表。
     *
     * @param labId  實驗室 ID
     * @param status 專案狀態
     * @return 專案列表
     */
    List<ProjectEntity> findByLabIdAndStatus(UUID labId, ProjectStatus status);

    /**
     * 根據實驗室 ID 查詢所有專案。
     *
     * @param labId 實驗室 ID
     * @return 專案列表
     */
    List<ProjectEntity> findByLabId(UUID labId);
}

package com.loveratory.experiment.repository;

import com.loveratory.experiment.entity.ExperimentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 實驗 Repository。
 */
public interface ExperimentRepository extends JpaRepository<ExperimentEntity, UUID>,
        JpaSpecificationExecutor<ExperimentEntity> {

    /**
     * 根據專案 ID 查詢所有實驗。
     *
     * @param projectId 專案 ID
     * @return 實驗列表
     */
    List<ExperimentEntity> findByProjectId(UUID projectId);

    /**
     * 根據 slug 查詢實驗。
     *
     * @param slug 實驗公開連結代碼
     * @return 實驗 Optional
     */
    Optional<ExperimentEntity> findBySlug(String slug);

    /**
     * 檢查 slug 是否已存在。
     *
     * @param slug 實驗公開連結代碼
     * @return 是否已存在
     */
    boolean existsBySlug(String slug);
}

package com.loveratory.experiment.manager;

import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.experiment.entity.ExperimentEntity;
import com.loveratory.experiment.repository.ExperimentRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 實驗資料存取管理器。
 * 封裝 ExperimentEntity 的所有 CRUD 操作。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ExperimentManager {

    private final ExperimentRepository experimentRepository;

    /**
     * 根據 ID 查詢實驗，找不到時拋出 BusinessException。
     *
     * @param experimentId 實驗 ID
     * @return 實驗 Entity
     */
    public ExperimentEntity findByIdOrThrow(@NonNull UUID experimentId) {
        return experimentRepository.findById(experimentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPERIMENT_NOT_FOUND));
    }

    /**
     * 根據專案 ID 查詢所有實驗。
     *
     * @param projectId 專案 ID
     * @return 實驗列表
     */
    public List<ExperimentEntity> findByProjectId(@NonNull UUID projectId) {
        return experimentRepository.findByProjectId(projectId);
    }

    /**
     * 根據 slug 查詢實驗。
     *
     * @param slug 實驗公開連結代碼
     * @return 實驗 Optional
     */
    public Optional<ExperimentEntity> findBySlug(@NonNull String slug) {
        return experimentRepository.findBySlug(slug);
    }

    /**
     * 檢查 slug 是否已存在。
     *
     * @param slug 實驗公開連結代碼
     * @return 是否已存在
     */
    public boolean existsBySlug(@NonNull String slug) {
        return experimentRepository.existsBySlug(slug);
    }

    /**
     * 儲存實驗。
     *
     * @param experimentEntity 實驗 Entity
     * @return 儲存後的實驗 Entity
     */
    public ExperimentEntity save(@NonNull ExperimentEntity experimentEntity) {
        return experimentRepository.save(experimentEntity);
    }
}

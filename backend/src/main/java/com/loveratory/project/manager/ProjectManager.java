package com.loveratory.project.manager;

import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.project.entity.ProjectEntity;
import com.loveratory.project.entity.ProjectStatus;
import com.loveratory.project.repository.ProjectRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 專案資料存取管理器。
 * 封裝 ProjectEntity 的所有 CRUD 操作。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ProjectManager {

    private final ProjectRepository projectRepository;

    /**
     * 根據 ID 查詢專案，找不到時拋出 BusinessException。
     *
     * @param projectId 專案 ID
     * @return 專案 Entity
     */
    public ProjectEntity findByIdOrThrow(@NonNull UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    /**
     * 根據實驗室 ID 與狀態查詢專案列表。
     *
     * @param labId  實驗室 ID
     * @param status 專案狀態
     * @return 專案列表
     */
    public List<ProjectEntity> findByLabId(@NonNull UUID labId, @NonNull ProjectStatus status) {
        return projectRepository.findByLabIdAndStatus(labId, status);
    }

    /**
     * 根據實驗室 ID 查詢所有專案。
     *
     * @param labId 實驗室 ID
     * @return 專案列表
     */
    public List<ProjectEntity> findByLabId(@NonNull UUID labId) {
        return projectRepository.findByLabId(labId);
    }

    /**
     * 儲存專案。
     *
     * @param projectEntity 專案 Entity
     * @return 儲存後的專案 Entity
     */
    public ProjectEntity save(@NonNull ProjectEntity projectEntity) {
        return projectRepository.save(projectEntity);
    }
}

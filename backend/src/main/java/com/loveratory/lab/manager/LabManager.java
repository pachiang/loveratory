package com.loveratory.lab.manager;

import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.lab.entity.LabEntity;
import com.loveratory.lab.repository.LabRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 實驗室資料存取管理器。
 * 封裝 LabEntity 的所有 CRUD 操作。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class LabManager {

    private final LabRepository labRepository;

    /**
     * 根據 ID 查詢實驗室，找不到時拋出 BusinessException。
     *
     * @param labId 實驗室 ID
     * @return 實驗室 Entity
     */
    public LabEntity findByIdOrThrow(@NonNull UUID labId) {
        return labRepository.findById(labId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LAB_NOT_FOUND));
    }

    /**
     * 根據實驗室代碼查詢實驗室。
     *
     * @param code 實驗室代碼
     * @return 實驗室 Optional
     */
    public Optional<LabEntity> findByCode(@NonNull String code) {
        return labRepository.findByCode(code);
    }

    public List<LabEntity> findByAppliedBy(@NonNull UUID appliedBy) {
        return labRepository.findByAppliedByOrderByCreatedAtDesc(appliedBy);
    }

    /**
     * 檢查實驗室代碼是否已存在。
     *
     * @param code 實驗室代碼
     * @return 是否已存在
     */
    public boolean existsByCode(@NonNull String code) {
        return labRepository.existsByCode(code);
    }

    /**
     * 儲存實驗室。
     *
     * @param labEntity 實驗室 Entity
     * @return 儲存後的實驗室 Entity
     */
    public LabEntity save(@NonNull LabEntity labEntity) {
        return labRepository.save(labEntity);
    }

    /**
     * 依查詢條件搜尋實驗室（分頁）。
     *
     * @param specification 查詢條件
     * @param pageable      分頁參數
     * @return 實驗室分頁結果
     */
    public Page<LabEntity> findAll(@NonNull Specification<LabEntity> specification,
                                   @NonNull Pageable pageable) {
        return labRepository.findAll(specification, pageable);
    }
}

package com.loveratory.lab.repository;

import com.loveratory.lab.entity.LabEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 實驗室 Repository。
 */
public interface LabRepository extends JpaRepository<LabEntity, UUID>,
        JpaSpecificationExecutor<LabEntity> {

    /**
     * 根據實驗室代碼查詢實驗室。
     *
     * @param code 實驗室代碼
     * @return 實驗室 Optional
     */
    Optional<LabEntity> findByCode(String code);

    List<LabEntity> findByAppliedByOrderByCreatedAtDesc(UUID appliedBy);

    /**
     * 檢查實驗室代碼是否已存在。
     *
     * @param code 實驗室代碼
     * @return 是否已存在
     */
    boolean existsByCode(String code);
}

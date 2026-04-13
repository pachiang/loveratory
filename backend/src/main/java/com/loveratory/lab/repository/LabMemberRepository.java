package com.loveratory.lab.repository;

import com.loveratory.lab.entity.LabMemberEntity;
import com.loveratory.lab.entity.LabMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 實驗室成員 Repository。
 */
public interface LabMemberRepository extends JpaRepository<LabMemberEntity, UUID>,
        JpaSpecificationExecutor<LabMemberEntity> {

    /**
     * 根據實驗室 ID 與使用者 ID 查詢成員。
     *
     * @param labId  實驗室 ID
     * @param userId 使用者 ID
     * @return 成員 Optional
     */
    Optional<LabMemberEntity> findByLabIdAndUserId(UUID labId, UUID userId);

    Optional<LabMemberEntity> findByLabIdAndUserIdAndStatus(UUID labId, UUID userId, LabMemberStatus status);

    /**
     * 根據實驗室 ID 與狀態查詢成員列表。
     *
     * @param labId  實驗室 ID
     * @param status 成員狀態
     * @return 成員列表
     */
    List<LabMemberEntity> findByLabIdAndStatus(UUID labId, LabMemberStatus status);

    /**
     * 根據使用者 ID 與狀態查詢成員列表。
     *
     * @param userId 使用者 ID
     * @param status 成員狀態
     * @return 成員列表
     */
    List<LabMemberEntity> findByUserIdAndStatus(UUID userId, LabMemberStatus status);

    /**
     * 檢查指定實驗室中是否存在指定狀態的成員。
     *
     * @param labId  實驗室 ID
     * @param userId 使用者 ID
     * @param status 成員狀態
     * @return 是否存在
     */
    boolean existsByLabIdAndUserIdAndStatus(UUID labId, UUID userId, LabMemberStatus status);
}

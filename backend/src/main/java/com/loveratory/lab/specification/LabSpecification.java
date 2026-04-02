package com.loveratory.lab.specification;

import com.loveratory.lab.entity.LabEntity;
import com.loveratory.lab.entity.LabStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * 實驗室查詢條件 Specification。
 * 所有 LabEntity 的動態查詢條件集中在此。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LabSpecification {

    /**
     * 依實驗室狀態查詢。
     *
     * @param status 實驗室狀態
     * @return Specification
     */
    public static Specification<LabEntity> hasStatus(LabStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(LabEntity.Fields.status), status);
    }

    /**
     * 依申請人 ID 查詢。
     *
     * @param userId 申請人使用者 ID
     * @return Specification
     */
    public static Specification<LabEntity> appliedByUser(UUID userId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(LabEntity.Fields.appliedBy), userId);
    }

    /**
     * 依實驗室名稱模糊查詢。
     *
     * @param keyword 關鍵字
     * @return Specification
     */
    public static Specification<LabEntity> nameContains(String keyword) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(LabEntity.Fields.name), "%" + keyword + "%");
    }
}

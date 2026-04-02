package com.loveratory.experiment.specification;

import com.loveratory.experiment.entity.ExperimentEntity;
import com.loveratory.experiment.entity.ExperimentStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * 實驗查詢條件 Specification。
 * 所有 ExperimentEntity 的動態查詢條件集中在此。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExperimentSpecification {

    /**
     * 依所屬專案 ID 查詢。
     *
     * @param projectId 專案 ID
     * @return Specification
     */
    public static Specification<ExperimentEntity> belongsToProject(UUID projectId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(ExperimentEntity.Fields.projectId), projectId);
    }

    /**
     * 依實驗狀態查詢。
     *
     * @param status 實驗狀態
     * @return Specification
     */
    public static Specification<ExperimentEntity> hasStatus(ExperimentStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(ExperimentEntity.Fields.status), status);
    }

    /**
     * 依 slug 模糊查詢。
     *
     * @param keyword 關鍵字
     * @return Specification
     */
    public static Specification<ExperimentEntity> slugContains(String keyword) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(ExperimentEntity.Fields.slug), "%" + keyword + "%");
    }
}

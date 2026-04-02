package com.loveratory.project.dto.response;

import com.loveratory.project.entity.ProjectEntity;
import com.loveratory.project.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 專案摘要回應。
 * 用於列表查詢，包含專案基本資訊。
 */
@Getter
@Builder
@Schema(description = "專案摘要回應")
public class ProjectSummaryResponse {

    @Schema(description = "專案 ID")
    private final UUID projectId;

    @Schema(description = "專案名稱")
    private final String name;

    @Schema(description = "專案狀態")
    private final ProjectStatus status;

    @Schema(description = "建立時間")
    private final ZonedDateTime createdAt;

    /**
     * 從 ProjectEntity 建立摘要回應。
     *
     * @param entity 專案 Entity
     * @return 專案摘要回應
     */
    public static ProjectSummaryResponse fromEntity(@NonNull ProjectEntity entity) {
        return ProjectSummaryResponse.builder()
                .projectId(entity.getId())
                .name(entity.getName())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

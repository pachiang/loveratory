package com.loveratory.project.dto.response;

import com.loveratory.project.entity.ProjectEntity;
import com.loveratory.project.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 專案詳情回應。
 * 包含專案完整資訊、建立者名稱與主持人列表。
 */
@Getter
@Builder
@Schema(description = "專案詳情回應")
public class ProjectDetailResponse {

    @Schema(description = "專案 ID")
    private final UUID projectId;

    @Schema(description = "實驗室 ID")
    private final UUID labId;

    @Schema(description = "專案名稱")
    private final String name;

    @Schema(description = "專案描述")
    private final String description;

    @Schema(description = "專案狀態")
    private final ProjectStatus status;

    @Schema(description = "建立者 ID")
    private final UUID createdBy;

    @Schema(description = "建立者姓名")
    private final String createdByName;

    @Schema(description = "建立時間")
    private final ZonedDateTime createdAt;

    @Schema(description = "主持人列表")
    private final List<InvestigatorResponse> investigators;

    /**
     * 從 ProjectEntity、建立者名稱與主持人列表建立詳情回應。
     *
     * @param entity        專案 Entity
     * @param createdByName 建立者姓名
     * @param investigators 主持人回應列表
     * @return 專案詳情回應
     */
    public static ProjectDetailResponse fromEntity(@NonNull ProjectEntity entity,
                                                   @NonNull String createdByName,
                                                   @NonNull List<InvestigatorResponse> investigators) {
        return ProjectDetailResponse.builder()
                .projectId(entity.getId())
                .labId(entity.getLabId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdBy(entity.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(entity.getCreatedAt())
                .investigators(investigators)
                .build();
    }
}

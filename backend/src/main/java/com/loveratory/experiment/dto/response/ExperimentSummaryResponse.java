package com.loveratory.experiment.dto.response;

import com.loveratory.experiment.entity.ExperimentEntity;
import com.loveratory.experiment.entity.ExperimentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 實驗摘要回應。
 * 用於列表查詢，僅包含實驗基本資訊。
 */
@Getter
@Builder
@Schema(description = "實驗摘要回應")
public class ExperimentSummaryResponse {

    @Schema(description = "實驗 ID")
    private final UUID experimentId;

    @Schema(description = "實驗名稱")
    private final String name;

    @Schema(description = "公開報名連結代碼")
    private final String slug;

    @Schema(description = "實驗狀態")
    private final ExperimentStatus status;

    @Schema(description = "每次時段長度（分鐘）")
    private final Integer durationMinutes;

    @Schema(description = "建立時間")
    private final ZonedDateTime createdAt;

    /**
     * 從 ExperimentEntity 建立摘要回應。
     *
     * @param entity 實驗 Entity
     * @return 實驗摘要回應
     */
    public static ExperimentSummaryResponse fromEntity(@NonNull ExperimentEntity entity) {
        return ExperimentSummaryResponse.builder()
                .experimentId(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .status(entity.getStatus())
                .durationMinutes(entity.getDurationMinutes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

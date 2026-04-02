package com.loveratory.experiment.dto.response;

import com.loveratory.experiment.dto.internal.FormConfig;
import com.loveratory.experiment.dto.internal.NotificationConfig;
import com.loveratory.experiment.entity.ExperimentEntity;
import com.loveratory.experiment.entity.ExperimentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 實驗詳情回應。
 * 包含實驗完整資訊與建立者名稱。
 */
@Getter
@Builder
@Schema(description = "實驗詳情回應")
public class ExperimentDetailResponse {

    @Schema(description = "實驗 ID")
    private final UUID experimentId;

    @Schema(description = "專案 ID")
    private final UUID projectId;

    @Schema(description = "實驗名稱")
    private final String name;

    @Schema(description = "實驗說明")
    private final String description;

    @Schema(description = "實驗地點")
    private final String location;

    @Schema(description = "每次時段長度（分鐘）")
    private final Integer durationMinutes;

    @Schema(description = "每個時段可報名人數")
    private final Integer maxParticipantsPerSlot;

    @Schema(description = "公開報名連結代碼")
    private final String slug;

    @Schema(description = "實驗狀態")
    private final ExperimentStatus status;

    @Schema(description = "是否允許同一 Email 重複報名")
    private final Boolean allowDuplicateEmail;

    @Schema(description = "是否允許受試者自行取消報名")
    private final Boolean allowParticipantCancel;

    @Schema(description = "報名表單欄位設定")
    private final FormConfig formConfig;

    @Schema(description = "通知設定")
    private final NotificationConfig notificationConfig;

    @Schema(description = "建立者 ID")
    private final UUID createdBy;

    @Schema(description = "建立者姓名")
    private final String createdByName;

    @Schema(description = "建立時間")
    private final ZonedDateTime createdAt;

    /**
     * 從 ExperimentEntity 與建立者名稱建立回應。
     *
     * @param entity        實驗 Entity
     * @param createdByName 建立者姓名
     * @return 實驗詳情回應
     */
    public static ExperimentDetailResponse fromEntity(@NonNull ExperimentEntity entity,
                                                      @NonNull String createdByName) {
        return ExperimentDetailResponse.builder()
                .experimentId(entity.getId())
                .projectId(entity.getProjectId())
                .name(entity.getName())
                .description(entity.getDescription())
                .location(entity.getLocation())
                .durationMinutes(entity.getDurationMinutes())
                .maxParticipantsPerSlot(entity.getMaxParticipantsPerSlot())
                .slug(entity.getSlug())
                .status(entity.getStatus())
                .allowDuplicateEmail(entity.getAllowDuplicateEmail())
                .allowParticipantCancel(entity.getAllowParticipantCancel())
                .formConfig(entity.getFormConfig())
                .notificationConfig(entity.getNotificationConfig())
                .createdBy(entity.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

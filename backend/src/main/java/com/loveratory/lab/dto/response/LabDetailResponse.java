package com.loveratory.lab.dto.response;

import com.loveratory.lab.entity.LabEntity;
import com.loveratory.lab.entity.LabStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 實驗室詳情回應。
 * 包含實驗室完整資訊與申請人名稱。
 */
@Getter
@Builder
@Schema(description = "實驗室詳情回應")
public class LabDetailResponse {

    @Schema(description = "實驗室 ID")
    private final UUID labId;

    @Schema(description = "實驗室名稱")
    private final String name;

    @Schema(description = "實驗室代碼")
    private final String code;

    @Schema(description = "實驗室描述")
    private final String description;

    @Schema(description = "實驗室狀態")
    private final LabStatus status;

    @Schema(description = "申請人 ID")
    private final UUID appliedBy;

    @Schema(description = "申請人姓名")
    private final String appliedByName;

    @Schema(description = "審核人 ID")
    private final UUID reviewedBy;

    @Schema(description = "審核備註")
    private final String reviewNote;

    @Schema(description = "核准時間")
    private final ZonedDateTime approvedAt;

    @Schema(description = "建立時間")
    private final ZonedDateTime createdAt;

    /**
     * 從 LabEntity 與申請人名稱建立回應。
     *
     * @param entity        實驗室 Entity
     * @param appliedByName 申請人姓名
     * @return 實驗室詳情回應
     */
    public static LabDetailResponse fromEntity(@NonNull LabEntity entity,
                                               @NonNull String appliedByName) {
        return LabDetailResponse.builder()
                .labId(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .appliedBy(entity.getAppliedBy())
                .appliedByName(appliedByName)
                .reviewedBy(entity.getReviewedBy())
                .reviewNote(entity.getReviewNote())
                .approvedAt(entity.getApprovedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

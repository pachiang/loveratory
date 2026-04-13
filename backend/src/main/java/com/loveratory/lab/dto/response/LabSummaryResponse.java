package com.loveratory.lab.dto.response;

import com.loveratory.lab.entity.LabEntity;
import com.loveratory.lab.entity.LabMemberRole;
import com.loveratory.lab.entity.LabStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 實驗室摘要回應。
 * 包含實驗室基本資訊與當前使用者的角色。
 */
@Getter
@Builder
@Schema(description = "實驗室摘要回應")
public class LabSummaryResponse {

    @Schema(description = "實驗室 ID")
    private final UUID labId;

    @Schema(description = "實驗室名稱")
    private final String name;

    @Schema(description = "實驗室代碼")
    private final String code;

    @Schema(description = "實驗室狀態")
    private final LabStatus status;

    @Schema(description = "當前使用者在此實驗室的角色")
    private final LabMemberRole myRole;

    @Schema(description = "建立時間")
    private final ZonedDateTime createdAt;

    /**
     * 從 LabEntity 與使用者角色建立摘要回應。
     *
     * @param entity 實驗室 Entity
     * @param myRole 當前使用者的角色
     * @return 實驗室摘要回應
     */
    public static LabSummaryResponse of(@NonNull LabEntity entity,
                                        @NonNull LabMemberRole myRole) {
        return LabSummaryResponse.builder()
                .labId(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .status(entity.getStatus())
                .myRole(myRole)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static LabSummaryResponse of(@NonNull LabEntity entity) {
        return LabSummaryResponse.builder()
                .labId(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .status(entity.getStatus())
                .myRole(null)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

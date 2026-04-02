package com.loveratory.project.dto.response;

import com.loveratory.auth.entity.UserEntity;
import com.loveratory.project.entity.ProjectInvestigatorEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 專案主持人回應。
 * 包含主持人的使用者資訊與加入時間。
 */
@Getter
@Builder
@Schema(description = "專案主持人回應")
public class InvestigatorResponse {

    @Schema(description = "使用者 ID")
    private final UUID userId;

    @Schema(description = "使用者姓名")
    private final String name;

    @Schema(description = "使用者 Email")
    private final String email;

    @Schema(description = "加入時間")
    private final ZonedDateTime addedAt;

    /**
     * 從主持人 Entity 與使用者 Entity 建立回應。
     *
     * @param investigatorEntity 主持人 Entity
     * @param userEntity         使用者 Entity
     * @return 主持人回應
     */
    public static InvestigatorResponse of(@NonNull ProjectInvestigatorEntity investigatorEntity,
                                          @NonNull UserEntity userEntity) {
        return InvestigatorResponse.builder()
                .userId(userEntity.getId())
                .name(userEntity.getName())
                .email(userEntity.getEmail())
                .addedAt(investigatorEntity.getAddedAt())
                .build();
    }
}

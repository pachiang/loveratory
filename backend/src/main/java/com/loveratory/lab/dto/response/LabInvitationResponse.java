package com.loveratory.lab.dto.response;

import com.loveratory.lab.entity.LabInvitationEntity;
import com.loveratory.lab.entity.LabInvitationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 實驗室邀請回應。
 * 包含邀請詳細資訊與邀請人名稱。
 */
@Getter
@Builder
@Schema(description = "實驗室邀請回應")
public class LabInvitationResponse {

    @Schema(description = "邀請 ID")
    private final UUID invitationId;

    @Schema(description = "被邀請者 Email")
    private final String email;

    @Schema(description = "邀請狀態")
    private final LabInvitationStatus status;

    @Schema(description = "邀請人 ID")
    private final UUID invitedBy;

    @Schema(description = "邀請人姓名")
    private final String invitedByName;

    @Schema(description = "過期時間")
    private final ZonedDateTime expiresAt;

    @Schema(description = "接受時間")
    private final ZonedDateTime acceptedAt;

    @Schema(description = "建立時間")
    private final ZonedDateTime createdAt;

    /**
     * 從 LabInvitationEntity 與邀請人名稱建立回應。
     *
     * @param entity        邀請 Entity
     * @param invitedByName 邀請人姓名
     * @return 實驗室邀請回應
     */
    public static LabInvitationResponse of(@NonNull LabInvitationEntity entity,
                                           @NonNull String invitedByName) {
        return LabInvitationResponse.builder()
                .invitationId(entity.getId())
                .email(entity.getEmail())
                .status(entity.getStatus())
                .invitedBy(entity.getInvitedBy())
                .invitedByName(invitedByName)
                .expiresAt(entity.getExpiresAt())
                .acceptedAt(entity.getAcceptedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

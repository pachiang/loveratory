package com.loveratory.registration.dto.response;

import com.loveratory.experiment.entity.ExperimentEntity;
import com.loveratory.registration.entity.RegistrationEntity;
import com.loveratory.registration.entity.RegistrationStatus;
import com.loveratory.slot.entity.TimeSlotEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 報名結果回應（公開）。
 * 提供受試者報名後的確認資訊。
 */
@Getter
@Builder
@Schema(description = "報名結果回應（公開）")
public class PublicRegistrationResponse {

    @Schema(description = "報名 ID")
    private final UUID registrationId;

    @Schema(description = "受試者 Email")
    private final String email;

    @Schema(description = "受試者姓名")
    private final String name;

    @Schema(description = "時段開始時間")
    private final ZonedDateTime slotStartTime;

    @Schema(description = "時段結束時間")
    private final ZonedDateTime slotEndTime;

    @Schema(description = "實驗名稱")
    private final String experimentName;

    @Schema(description = "實驗地點")
    private final String location;

    @Schema(description = "報名狀態")
    private final RegistrationStatus status;

    @Schema(description = "取消 token（僅實驗允許取消時提供）")
    private final String cancelToken;

    @Schema(description = "報名時間")
    private final ZonedDateTime registeredAt;

    /**
     * 從報名、時段與實驗 Entity 建立公開回應。
     *
     * @param registration 報名 Entity
     * @param slot         時段 Entity
     * @param experiment   實驗 Entity
     * @return 公開報名回應
     */
    public static PublicRegistrationResponse of(@NonNull RegistrationEntity registration,
                                                 @NonNull TimeSlotEntity slot,
                                                 @NonNull ExperimentEntity experiment) {
        String cancelToken = Boolean.TRUE.equals(experiment.getAllowParticipantCancel())
                ? registration.getCancelToken()
                : null;

        return PublicRegistrationResponse.builder()
                .registrationId(registration.getId())
                .email(registration.getParticipantEmail())
                .name(registration.getParticipantName())
                .slotStartTime(slot.getStartTime())
                .slotEndTime(slot.getEndTime())
                .experimentName(experiment.getName())
                .location(experiment.getLocation())
                .status(registration.getStatus())
                .cancelToken(cancelToken)
                .registeredAt(registration.getRegisteredAt())
                .build();
    }
}

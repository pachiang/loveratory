package com.loveratory.registration.dto.response;

import com.loveratory.registration.entity.RegistrationEntity;
import com.loveratory.registration.entity.RegistrationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 報名回應（後台管理用）。
 */
@Getter
@Builder
@Schema(description = "報名回應")
public class RegistrationResponse {

    @Schema(description = "報名 ID")
    private final UUID registrationId;

    @Schema(description = "時段 ID")
    private final UUID slotId;

    @Schema(description = "受試者 Email")
    private final String participantEmail;

    @Schema(description = "受試者姓名")
    private final String participantName;

    @Schema(description = "受試者手機")
    private final String participantPhone;

    @Schema(description = "受試者學號")
    private final String participantStudentId;

    @Schema(description = "受試者年齡")
    private final Integer participantAge;

    @Schema(description = "受試者性別")
    private final String participantGender;

    @Schema(description = "受試者慣用手")
    private final String participantDominantHand;

    @Schema(description = "備註")
    private final String participantNotes;

    @Schema(description = "報名狀態")
    private final RegistrationStatus status;

    @Schema(description = "報名時間")
    private final ZonedDateTime registeredAt;

    @Schema(description = "取消時間")
    private final ZonedDateTime cancelledAt;

    /**
     * 從 Entity 建立 Response。
     *
     * @param entity 報名 Entity
     * @return 報名回應
     */
    public static RegistrationResponse fromEntity(@NonNull RegistrationEntity entity) {
        return RegistrationResponse.builder()
                .registrationId(entity.getId())
                .slotId(entity.getTimeSlotId())
                .participantEmail(entity.getParticipantEmail())
                .participantName(entity.getParticipantName())
                .participantPhone(entity.getParticipantPhone())
                .participantStudentId(entity.getParticipantStudentId())
                .participantAge(entity.getParticipantAge())
                .participantGender(entity.getParticipantGender())
                .participantDominantHand(entity.getParticipantDominantHand())
                .participantNotes(entity.getParticipantNotes())
                .status(entity.getStatus())
                .registeredAt(entity.getRegisteredAt())
                .cancelledAt(entity.getCancelledAt())
                .build();
    }
}

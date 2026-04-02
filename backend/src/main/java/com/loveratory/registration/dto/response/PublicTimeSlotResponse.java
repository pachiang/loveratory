package com.loveratory.registration.dto.response;

import com.loveratory.slot.entity.TimeSlotEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 公開時段資訊。
 * 提供受試者可見的時段資訊，包含剩餘名額。
 */
@Getter
@Builder
@Schema(description = "公開時段資訊")
public class PublicTimeSlotResponse {

    @Schema(description = "時段 ID")
    private final UUID slotId;

    @Schema(description = "開始時間")
    private final ZonedDateTime startTime;

    @Schema(description = "結束時間")
    private final ZonedDateTime endTime;

    @Schema(description = "可報名人數")
    private final Integer capacity;

    @Schema(description = "目前已報名人數")
    private final Integer currentCount;

    @Schema(description = "剩餘名額")
    private final Integer remainingSpots;

    /**
     * 從 Entity 建立公開時段回應。
     *
     * @param entity 時段 Entity
     * @return 公開時段回應
     */
    public static PublicTimeSlotResponse fromEntity(@NonNull TimeSlotEntity entity) {
        return PublicTimeSlotResponse.builder()
                .slotId(entity.getId())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .capacity(entity.getCapacity())
                .currentCount(entity.getCurrentCount())
                .remainingSpots(entity.getCapacity() - entity.getCurrentCount())
                .build();
    }
}

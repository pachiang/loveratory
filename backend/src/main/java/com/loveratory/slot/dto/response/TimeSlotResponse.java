package com.loveratory.slot.dto.response;

import com.loveratory.slot.entity.TimeSlotEntity;
import com.loveratory.slot.entity.TimeSlotStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 時段回應。
 */
@Getter
@Builder
@Schema(description = "時段回應")
public class TimeSlotResponse {

    @Schema(description = "時段 ID")
    private final UUID slotId;

    @Schema(description = "實驗 ID")
    private final UUID experimentId;

    @Schema(description = "開始時間")
    private final ZonedDateTime startTime;

    @Schema(description = "結束時間")
    private final ZonedDateTime endTime;

    @Schema(description = "可報名人數")
    private final Integer capacity;

    @Schema(description = "目前已報名人數")
    private final Integer currentCount;

    @Schema(description = "時段狀態")
    private final TimeSlotStatus status;

    @Schema(description = "建立時間")
    private final ZonedDateTime createdAt;

    /**
     * 從 Entity 建立 Response。
     *
     * @param entity 時段 Entity
     * @return 時段回應
     */
    public static TimeSlotResponse fromEntity(@NonNull TimeSlotEntity entity) {
        return TimeSlotResponse.builder()
                .slotId(entity.getId())
                .experimentId(entity.getExperimentId())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .capacity(entity.getCapacity())
                .currentCount(entity.getCurrentCount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

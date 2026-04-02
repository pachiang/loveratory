package com.loveratory.slot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

/**
 * 更新時段請求。
 */
@Getter
@Setter
@Schema(description = "更新時段請求")
public class SlotUpdateRequest {

    @NotNull(message = "開始時間不可為空")
    @Schema(description = "開始時間", example = "2026-04-06T09:00:00+08:00")
    private ZonedDateTime startTime;

    @NotNull(message = "結束時間不可為空")
    @Schema(description = "結束時間", example = "2026-04-06T09:30:00+08:00")
    private ZonedDateTime endTime;

    @NotNull(message = "可報名人數不可為空")
    @Schema(description = "可報名人數", example = "5")
    private Integer capacity;
}

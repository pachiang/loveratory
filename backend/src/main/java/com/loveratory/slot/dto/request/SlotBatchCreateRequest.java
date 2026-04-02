package com.loveratory.slot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 批次建立時段請求。
 * 根據日期範圍、星期、每日時段區間等參數自動產生多個時段。
 */
@Getter
@Setter
@Schema(description = "批次建立時段請求")
public class SlotBatchCreateRequest {

    @NotNull(message = "日期範圍起始不可為空")
    @Schema(description = "日期範圍起始", example = "2026-04-06")
    private LocalDate startDate;

    @NotNull(message = "日期範圍結束不可為空")
    @Schema(description = "日期範圍結束", example = "2026-04-10")
    private LocalDate endDate;

    @NotEmpty(message = "星期幾有時段不可為空")
    @Schema(description = "星期幾有時段", example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]")
    private List<DayOfWeek> daysOfWeek;

    @NotNull(message = "每天起始時間不可為空")
    @Schema(description = "每天起始時間", example = "09:00")
    private LocalTime dailyStartTime;

    @NotNull(message = "每天結束時間不可為空")
    @Schema(description = "每天結束時間", example = "17:00")
    private LocalTime dailyEndTime;

    @NotNull(message = "每次時段長度不可為空")
    @Min(value = 1, message = "每次時段長度至少為 1 分鐘")
    @Schema(description = "每次時段長度（分鐘）", example = "30")
    private Integer durationMinutes;

    @Min(value = 0, message = "時段間休息不可為負數")
    @Schema(description = "時段間休息（分鐘），預設 0", example = "10")
    private Integer breakMinutes = 0;

    @Schema(description = "可報名人數（未填則使用實驗設定）", example = "5")
    private Integer capacity;
}

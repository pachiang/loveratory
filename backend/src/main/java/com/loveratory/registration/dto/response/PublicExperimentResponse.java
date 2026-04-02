package com.loveratory.registration.dto.response;

import com.loveratory.experiment.dto.internal.FormConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 公開實驗資訊（受試者視角）。
 * 提供受試者報名頁面所需的實驗資訊與可報名時段。
 */
@Getter
@Builder
@Schema(description = "公開實驗資訊（受試者視角）")
public class PublicExperimentResponse {

    @Schema(description = "實驗名稱")
    private final String name;

    @Schema(description = "實驗說明")
    private final String description;

    @Schema(description = "實驗地點")
    private final String location;

    @Schema(description = "每次時段長度（分鐘）")
    private final Integer durationMinutes;

    @Schema(description = "報名表單欄位設定")
    private final FormConfig formConfig;

    @Schema(description = "可報名時段列表")
    private final List<PublicTimeSlotResponse> availableSlots;
}

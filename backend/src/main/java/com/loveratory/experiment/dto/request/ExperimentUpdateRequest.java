package com.loveratory.experiment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新實驗請求。
 */
@Getter
@Setter
@Schema(description = "更新實驗請求")
public class ExperimentUpdateRequest {

    @NotBlank(message = "實驗名稱不可為空")
    @Size(max = 200, message = "實驗名稱長度不可超過 200 字元")
    @Schema(description = "實驗名稱", example = "前測 - Stroop Task")
    private String name;

    @Schema(description = "實驗說明（給受試者看的描述）", example = "本實驗約需 30 分鐘，請準時到場")
    private String description;

    @Schema(description = "實驗地點", example = "理學院 B101")
    private String location;

    @NotNull(message = "時段長度不可為空")
    @Min(value = 1, message = "時段長度至少為 1 分鐘")
    @Schema(description = "每次時段長度（分鐘）", example = "30")
    private Integer durationMinutes;

    @NotNull(message = "每時段最大報名人數不可為空")
    @Min(value = 1, message = "每時段最大報名人數至少為 1")
    @Schema(description = "每個時段可報名人數", example = "5")
    private Integer maxParticipantsPerSlot;

    @Schema(description = "是否允許同一 Email 重複報名")
    private Boolean allowDuplicateEmail;

    @Schema(description = "是否允許受試者自行取消報名（OPEN 狀態後不可更改）")
    private Boolean allowParticipantCancel;
}

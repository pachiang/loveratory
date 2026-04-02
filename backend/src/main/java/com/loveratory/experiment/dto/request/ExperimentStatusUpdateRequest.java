package com.loveratory.experiment.dto.request;

import com.loveratory.experiment.entity.ExperimentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新實驗狀態請求。
 */
@Getter
@Setter
@Schema(description = "更新實驗狀態請求")
public class ExperimentStatusUpdateRequest {

    @NotNull(message = "實驗狀態不可為空")
    @Schema(description = "實驗狀態", example = "OPEN")
    private ExperimentStatus status;
}

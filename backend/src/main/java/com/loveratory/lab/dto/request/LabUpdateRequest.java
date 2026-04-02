package com.loveratory.lab.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新實驗室請求。
 */
@Getter
@Setter
@Schema(description = "更新實驗室請求")
public class LabUpdateRequest {

    @NotBlank(message = "實驗室名稱不可為空")
    @Schema(description = "實驗室名稱", example = "認知心理學實驗室")
    private String name;

    @Schema(description = "實驗室描述", example = "專注於認知心理學相關研究")
    private String description;
}

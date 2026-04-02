package com.loveratory.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 建立專案請求。
 */
@Getter
@Setter
@Schema(description = "建立專案請求")
public class ProjectCreateRequest {

    @NotBlank(message = "專案名稱不可為空")
    @Size(max = 200, message = "專案名稱長度不可超過 200 字元")
    @Schema(description = "專案名稱", example = "注意力與工作記憶研究")
    private String name;

    @Schema(description = "專案描述（選填）", example = "探討注意力與工作記憶之間的關聯性")
    private String description;
}

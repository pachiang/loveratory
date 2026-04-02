package com.loveratory.lab.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 建立實驗室請求。
 */
@Getter
@Setter
@Schema(description = "建立實驗室請求")
public class LabCreateRequest {

    @NotBlank(message = "實驗室名稱不可為空")
    @Size(max = 200, message = "實驗室名稱長度不可超過 200 字元")
    @Schema(description = "實驗室名稱", example = "認知心理學實驗室")
    private String name;

    @NotBlank(message = "實驗室代碼不可為空")
    @Size(max = 100, message = "實驗室代碼長度不可超過 100 字元")
    @Pattern(regexp = "[a-z0-9-]+", message = "實驗室代碼只能包含小寫英文、數字與連字號")
    @Schema(description = "實驗室代碼", example = "cog-psy-lab")
    private String code;

    @Schema(description = "實驗室描述（選填）", example = "專注於認知心理學相關研究")
    private String description;
}
